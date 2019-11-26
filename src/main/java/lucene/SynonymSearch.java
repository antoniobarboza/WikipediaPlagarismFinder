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
import java.util.ArrayList;

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

/** Simple command-line based search demo. */
public class SynonymSearch {
	
  private SynonymSearch() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    //This is a directory to the index
    String indexPath = "./src/main/java/index";
    
    SynonymFinder synFind = new SynonymFinder();
    synFind.findSyn("a");
    
    //Code to get the argument string 
    String queryString = "";
    for (int i=0; i<args.length; i ++) {
        queryString += args[i] + " ";
    }
    ArrayList<String> queries = new ArrayList<String>();
    queries.add("Basketball is a non-contact sport played on a rectangular court.");
    //queries.add("whale vocalization production of sound");
    //queries.add("pokemon puzzle league");
    
    
    if(queryString != "") queries.add(queryString);
    
    try {
    	for(int i = 0;i < queries.size(); i++) {
    		if(i != 0) System.out.print("\n\n\n");
    		runSearch(queries.get(i), indexPath);
    	}
    } catch(Exception e) {
    	System.out.println("Query failed! " + e.getMessage());
    }
    
  }
  
  private static void runSearch(String queryString, String indexPath) throws Exception {
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
	    TopDocs searchResult = searcher.search(query,3);
	    ScoreDoc[] hits = searchResult.scoreDocs;
	    
	    //System.out.println("Results found: " + searchResult.totalHits);
	    
	    //If there are no results
	    if (hits.length == 0) {
	        System.out.println("No result found for: " + queryString);   	
	    }
	    else {
	    	System.out.println("Results for query: " + queryString);
	    }
	    //Instead of just displaying the contents.. I need to see how much of the input String is copied.
	    
	    ArrayList<Double> Pscores = new ArrayList<Double>();
	    
	    for (int j=0; j < hits.length; j++ ) {
	    	Document document = searcher.doc(hits[j].doc);
	    	String id = document.get("id");
	    	String text = document.get("text").toString();
	    	//System.out.println("Page ID: " + id + ":\nContents: " + text);
	    	Pscores.add(calculatePlagarismNaive( queryString, text ));
	    	//calculatePlagarism( queryString, text );
	    }
	    double high = 0.0;
	    for ( Double Pscore : Pscores ) {
	    	if ( high < Pscore ) {
	    		high = Pscore;
	    	}
		    
	    }
	    System.out.println( "The percent plagiarized on sentince by sentince basis: " + high );

  }
  private static double calculatePlagarismNaive( String queryString, String content ) {
	  //This function is going to look at he input string of the program and determine how much of it copied
	  //The group words variable will be used to group the total words that are used in the contains. 
	  
	  int totalWords = 0;
	  int sentinceMatches = 0;
	  for (String word : queryString.split("\\.s+")) {
		  //System.out.println("Sentince: " + word);
		  totalWords++; 
		  if ( content.contains(word)) {
			  sentinceMatches++;
		  }
	  }
	  double score = (double)sentinceMatches/(double)totalWords;
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