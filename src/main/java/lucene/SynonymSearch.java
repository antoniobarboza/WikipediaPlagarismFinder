/*
Se * Licensed to the Apache Software Foundation (ASF) under one or more
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
import java.util.Iterator;
import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


/** Simple command-line based search demo. */
public class SynonymSearch {
	
  private SynonymSearch() {}
  private static HashSet<String> stopWordMap;
  private static HashMap<String, HashSet<String>> wordMap;

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    long startTime = System.currentTimeMillis();
	boolean doubleSpace = true;
    //This is a directory to the index
    String indexPath = "./src/main/java/index";
    String qrelPath = "./src/main/java/QRELFiles";
    
    SynonymFinder synFind = new SynonymFinder();
    wordMap = new  HashMap<String, HashSet<String>>();
    
    //synFind.findSyn("a");
    //Here I am going to build the hashSet of StopWords 
    stopWordMap = DataManager.getStopWordsFromFile("./src/main/java/data/stopWords.txt");
    
    //Code to get the argument string 
    String queryString = "";
    for (int i=0; i<args.length; i ++) {
        queryString += args[i] + " ";
    }
    ArrayList<String> queries = new ArrayList<String>();
    //queries.add("Basketball is a non-contact sport played on a rectangular court.");
    //queries.add("whale vocalization production of sound");
    //queries.add("pokemon puzzle league")
    //System.out.println("Query: Basketball is a non-contact");
    if ( queryString.equals("")) {
    	queryString = "The ICC Cricket universe Cup is the international championship of One Day International (ODI) cricket.  The Second Den.  Non Plagarized";
    }
    if ( doubleSpace ) {
    	queryString = queryString.replaceAll("\\. ", "\\.  " );
    }
    System.out.println("Plagarism Application SynonymSearcher! Starting... \n\n");
    System.out.println("Running query: "+ queryString);
    
    System.out.println("Beginning query expansion..");
    queries = queryExpansion(queryString.toLowerCase());
    System.out.println("Done query expansion..");
    //At this point I have expanded all of the queries by 1 word syn 
    
    try {
    	Pair<Double, String> max = new Pair<Double, String>(0.0, "");
    	for(int i = 0;i < queries.size(); i++) {
    		if(i != 0) {
    			//System.out.print("\n\n\n");
    		}
    		Pair<Double, String> score = runSearch(queries.get(i), indexPath);
    		if ( score.fst > max.fst ) {
    			max = score;
    		}
    	}
    	DecimalFormat df = new DecimalFormat("0.00");
    	System.out.println("\nThe Plagarism Score calculated on a sentince by sentince basis with Syn Query Expansion = " + df.format(max.fst*100) + "% From Document: " + max.snd);
 
    } catch(Exception e) {
    	System.out.println("Query failed! " + e.getMessage());
    }
    long endTime = System.currentTimeMillis();
    long elap = endTime - startTime;
    
    //long secElap = TimeUnit.MILLISECONDS.toSeconds(elap);
    
    System.out.println("\n\nThe program took: " + elap +" milliseconds");
  }
  /**
   * This function is going to expand a given query
   * return ArrayList of expanded versions of the queries 
   * Ignore all stopWords and not expand those. 
   *  
   */
  private static ArrayList<String> queryExpansion(String queryString) {
	  ArrayList<String> queries = new ArrayList<String>();
	  queries.add(queryString); //add the original
	  String[] arr = queryString.split("\\s+");
	  //String curString = "";
	  for (int i =0; i < arr.length; i ++ ) {
		  String word = arr[i];
		  if (word == null) {
			  continue;
		  }
		  if ( !stopWordMap.contains(word) ) {
			  //Then it is not a stopword so I need to Loopup syn
			  wordMap.put(word, SynonymFinder.findSyn(word));
		  }
		  if( wordMap.containsKey(word) && wordMap.get(word) != null) {
			  //Add expanded query versions 
			  HashSet<String> temp = wordMap.get(word);
			  Iterator<String> it = temp.iterator();
			 
			  while(it.hasNext()) {
				  //Looping through each Syn for word
				  queries.add(queryString.replaceAll(word, it.next()));
			  }
		  }
		  //curString += word + " ";
	  }
	  return queries;
  }
  private static Pair<Double, String> runSearch(String queryString, String indexPath) throws Exception {
	    Directory dir = FSDirectory.open(Paths.get(indexPath));
	    IndexReader reader = DirectoryReader.open(dir);
	    
	  //track unique terms to enable our custom similarites
    	int termCount = 0;
        HashSet<String> uniqueTerms = new HashSet<String>();
        for (int i=0; i<reader.maxDoc(); i++) {
            //Document doc = reader.document(i);
            //String docID = doc.get("docId");
            int docID = i;
        	Terms terms = reader.getTermVector(docID, "text");
        	if(terms == null) continue;
        	TermsEnum termsIterator = terms.iterator();
            BytesRef byteRef = null;
            while ((byteRef = termsIterator.next()) != null) {
                //add each term to the unique terms count
            	//This will be used for the unigram LM and will be passed to our custom similarities
                String term =byteRef.utf8ToString();
                uniqueTerms.add(term);
                termCount += termsIterator.totalTermFreq();
            }
        }
	    IndexSearcher searcher = new IndexSearcher(reader);
	    searcher.setSimilarity(new BM25Similarity());
	    
	    //This sets up the query
	    Analyzer analyzer = new StandardAnalyzer();
	    QueryParser queryParser = new QueryParser("text", analyzer);
	    //Query query = queryParser.parse(QueryParser.escape(queryString));
	    Query query = queryParser.parse(queryString);
	    
	    //This initiates the search and returns top 10
	    //System.out.println("STARTING RETREVAl: " + query.toString());
	    TopDocs searchResult = searcher.search(query,1000);
	    ScoreDoc[] hits = searchResult.scoreDocs;
	    
	    //System.out.println("Results found: " + searchResult.totalHits);
	    
	    //If there are no results
	    if (hits.length == 0) {
	        //System.out.println("No result found for: " + queryString);   	
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
	    	Pscores.add(new Pair<Double, String>(calculatePlagarismNaive( queryString, text ), id));
	    	//calculatePlagarism( queryString, text );
	    }
	    Pair<Double, String> high = new Pair<Double, String>(0.0, "");
	    for ( Pair<Double, String> Pscore : Pscores ) {
	    	if ( high.fst < Pscore.fst ) {
	    		high = Pscore;
	    	}
		    
	    }
	    //System.out.println( "The percent plagiarized on sentince by sentince basis: " + high );
	    return high;

  }
  private static double calculatePlagarismNaive( String queryString, String content ) {
	  //This function is going to look at he input string of the program and determine how much of it copied
	  //The group words variable will be used to group the total words that are used in the contains. 
	  queryString = queryString.toLowerCase().trim();
	  content = content.toLowerCase();
	  content = content.replaceAll("['\"]", "");
	  content = content.replaceAll("\\. ", "\\.  " );
	  
	  int totalsent = 0;
	  int sentinceMatches = 0;
	  BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
	  String source = queryString;
	  iterator.setText(source);
	  int start = iterator.first();
	  //System.out.println("Content \n" + content);
	  for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
		  totalsent++;
		  String sent = source.substring(start,end);
		  //System.out.println(": " + sent);
		  if ( content.contains(sent.substring(0, sent.length()-1))) {
			  //System.out.println("MAtch");
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
				  //System.out.println("Consecutive");
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
