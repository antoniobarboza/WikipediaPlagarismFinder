/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.sun.tools.javac.util.Pair;

/** Simple command-line based search demo. */
public class SearchFiles {
	
  private SearchFiles() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
	boolean doubleSpace = true;
    //This is a directory to the index
    String indexPath = "./src/main/java/index";
    
    //LSH
    HashMap<String, ArrayList<String>> matches = null;
    //true if the locality sensitive hashing should be used false other wise
    boolean lsh = false;
    
    //This creates the hashMap of Id's -> list of matches based on threshold provided
    HashMap<String, HashSet<Integer>> allMinHashes = new HashMap<String, HashSet<Integer>>();
    if(lsh) {
    	Directory dir = FSDirectory.open(Paths.get(indexPath));
    	IndexReader reader = DirectoryReader.open(dir);
    	//Create the minHashes to be used
    	MinHash [] hashes = new MinHash[10];
    	for(int i = 0; i < hashes.length; i++) {
    		hashes[i] = new MinHash();
    		//System.out.println("MINHASH: " + hashes[i].toString());
    		
    	}
    	//loop through all docs and construct their min hashes
    	System.out.println("Creating shingles and generating min-hashes for all documents...");
    	//System.out.println("Num Docs: " + reader.maxDoc());
    	for (int i=0; i<reader.maxDoc(); i++) {
    		Document doc = reader.document(i);
    		String docText = doc.get("text");
    		//This is how you get the hashset of doc mins
    		HashSet<Integer> docMins = LSH.minHash(LSH.createShingles(docText), hashes);
		 
    		//need to add this so all hashsets are in a lsit
    		allMinHashes.put(doc.get("id"), docMins);
    		//allMinHashes.add(doc.get("id"), LSH.minHash(LSH.createShingles(docText), hashes));
    	}
    	System.out.println("Retrieving all documents with Jaccard Coefficient Greater than .7");
		matches = LSH.getDocIdsWithJaccardCoAtLeast(allMinHashes, (float) 0.7); 
    }
    
    //Code to get the argument string 
    String queryString = "";
    for (int i=0; i<args.length; i ++) {
        queryString += args[i] + " ";
    }
    ArrayList<String> queries = new ArrayList<String>();
    if ( queryString.equals("")) {
    	queryString = "The ICC Cricket World Cup is the international championship of One Day International (ODI) cricket.";
    }
    System.out.println( "Running the query: "+ queryString );
    if ( doubleSpace ) {
    	queryString = queryString.replaceAll("\\. ", "\\.  " );
    }
    System.out.println("Application Starting Basic Plagarism... \n");

    queries.add(queryString);
    
    //queries.add("whale vocalization production of sound");
    //queries.add("pokemon puzzle league");
   
    //if(queryString != "") queries.add(queryString);
    
    try {
    	for(int i = 0;i < queries.size(); i++) {
    		if(i != 0) System.out.print("\n\n\n");
    		runSearch(queries.get(i), indexPath, matches, lsh);
    	}
    } catch(Exception e) {
    	System.out.println("Query failed! " + e.getMessage());
    }
    
  }
  
  private static void runSearch(String queryString, String indexPath, HashMap<String, ArrayList<String>> matches, boolean lsh) throws Exception {
	    Directory dir = FSDirectory.open(Paths.get(indexPath));
	    IndexReader reader = DirectoryReader.open(dir);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    searcher.setSimilarity(new BM25Similarity());
	    
	    //This sets up the query
	    Analyzer analyzer = new StandardAnalyzer();
	    QueryParser queryParser = new QueryParser("text", analyzer);
	    //Query query = queryParser.parse(QueryParser.escape(queryString));
	    Query query = queryParser.parse(queryString);
	    
	    //This initiates the search and returns top 10
	    //System.out.println("STARTING RETREVAl: " + query.toString());
	    TopDocs searchResult = searcher.search(query,1);
	    ScoreDoc[] hits = searchResult.scoreDocs;
	    
	    //System.out.println("Results found: " + searchResult.totalHits);
	    
	    //If there are no results
	    if (hits.length == 0) {
	        System.out.println("No result found for: " + queryString);   	
	    }
	    else {
	    	//System.out.println("Results for query: " + queryString);
	    }
	    //Instead of just displaying the contents.. I need to see how much of the input String is copied.
	    
	    ArrayList<Pair<Double, String>> Pscores = new ArrayList<Pair<Double, String>>();

	    for (int j=0; j < hits.length; j++ ) {
	    	Document document = searcher.doc(hits[j].doc);
	    	String id = document.get("id");
	    	String text = document.get("text").toString();
	    	//System.out.println("Page ID: " + id + ":\nContents: " + text);
	    	Pair<Double, String> ins = new Pair<Double, String> (calculatePlagarismNaive( queryString, text ), id );
	    	Pscores.add(ins);
	    	
	    	//If we are using LSH this prints out all related docIDs below the docs
	    	if(lsh) {
	    		ArrayList<String> related = matches.get(id);
	    		if(related != null) {
	    			System.out.print("\t related doc IDS: ");
	    			for(String docID: related) {
	    				System.out.print(docID + " ");
	    			}
	    			System.out.println();
	    		}
	    	}
	    	//calculatePlagarism( queryString, text );
	    }
	    DecimalFormat df = new DecimalFormat("0.00");
	    double high = 0.0;
	    String curDoc = "";
	    for ( Pair<Double, String> Pscore : Pscores ) {
	    	if ( high < Pscore.fst ) {
	    		high = Pscore.fst;
	    		curDoc = Pscore.snd;
	    	}		    
	    }
	    System.out.println( "The percent plagiarized on sentence by sentence basis: " + df.format(high * 100) + "% From Document: " + curDoc );

  }
  private static double calculatePlagarismNaive( String queryString, String content ) {
	  //This function is going to look at he input string of the program and determine how much of it copied
	  //The group words variable will be used to group the total words that are used in the contains. 
	  queryString = queryString.toLowerCase();
	  content = content.toLowerCase();
	  content = content.replaceAll("['\"]", "");
	  
	  int totalsent = 0;
	  int sentinceMatches = 0;
	  BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
	  String source = queryString;
	  iterator.setText(source);
	  int start = iterator.first();
	  System.out.println("Content \n" + content);
	  for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
		  totalsent++;
		  String sent = source.substring(start,end);
		  System.out.println(": " + sent);
		  if ( content.contains(sent.substring(0, sent.length()-1))) {
			  System.out.println("MAtch");
			  sentinceMatches++;
		  }
	  }
	  double score = (double)sentinceMatches/(double)totalsent;
	  //System.out.println( "Plagarism Naive percent score: " + score*100 + "%");
	  return score;
  }
  
  
  private static double calculatePlagarism(String queryString, String content ) {
	  int totalWords = 0;
	  double wordMatches = 0;
	  boolean lastHit = false;
	  for (String word : queryString.split("\\s+")) {
		  totalWords++; 
		  if ( content.contains(word)) {
			  wordMatches += 1;
			  if( lastHit ) {
				  System.out.println("Consecutive");
				  wordMatches = wordMatches * 1.01; //Give a boost for a consecutive match;
			  }
			  lastHit = true;
		  }
		  else {
			  wordMatches = wordMatches * 0.9;
		  }
	  }
	  double score = wordMatches/(double)totalWords;
	  System.out.println( "Plagarism Weight percent score: " + score*100 + "%");
	  return score;
  }
}

  