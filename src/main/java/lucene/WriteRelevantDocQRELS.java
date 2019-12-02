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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
//import com.sun.tools.javac.util.Pair;

/** Simple command-line based search demo. */
public class WriteRelevantDocQRELS {
	
  private WriteRelevantDocQRELS() {}
  private static HashSet<String> stopWordMap = DataManager.getStopWordsFromFile("./src/main/java/data/stopWords.txt");
  //private static HashMap<String, HashSet<String>> wordMap;

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    //This is a directory to the index
    String indexPath = "./src/main/java/index";
    String qrelPath = "./src/main/java/qrelfile/qrels.txt";
    
    Files.deleteIfExists(Paths.get(qrelPath));
	File qrel = new File(qrelPath);
	qrel.createNewFile();
	BufferedWriter writer = new BufferedWriter(new FileWriter(qrelPath));
    
    //wordMap = new  HashMap<String, HashSet<String>>();
    
    //synFind.findSyn("a");
    //Here I am going to build the hashSet of StopWords 
    //stopWordMap = DataManager.getStopWordsFromFile("./src/main/java/data/stopWords.txt");
    
    System.out.println("Writing files with over 50% plagiarism scores for 25 queries...");
    ArrayList<String> queries = DataManager.get25Queries();
    //System.out.println("NUM QUERIES: " + queries.size());
    try {
    	for(String query: queries) {
    		runSearch(query, indexPath, writer);
    	}
    	//System.out.println("\nThe Plagarism Score calculated on a sentince by sentince basis with Syn Query Expansion = " + df.format(max.fst*100) + "% From Document: " + max.snd);
 
    } catch(Exception e) {
    	System.out.println("Query failed! " + e.getMessage());
    	e.printStackTrace();
    }
    System.out.println("QRELS have been written!");
    
  }
  private static void runSearch(String queryString, String indexPath, BufferedWriter writer) throws Exception {
	    Directory dir = FSDirectory.open(Paths.get(indexPath));
	    IndexReader reader = DirectoryReader.open(dir);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    searcher.setSimilarity(new BM25Similarity());
	    
	    //This sets up the query
	    Analyzer analyzer = new StandardAnalyzer();
	    QueryParser queryParser = new QueryParser("text", analyzer);
	    //Query query = queryParser.parse(QueryParser.escape(queryString));
	    Query query = queryParser.parse(QueryParser.escape(queryString));
	    
	    queryString = queryString.replaceAll("\\. ", "\\.  " );
		queryString = queryString.replaceAll("[\'\"]", "");
		//queryString = queryString.replaceAll("[-]", " ");
	    
	    //This initiates the search and returns top 10
	    //System.out.println("STARTING RETREVAl: " + query.toString());
	    TopDocs searchResult = searcher.search(query,20);
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
	    //System.out.println("hit length: " + hits.length);
	    //int count = 0;
	    for (int j=0; j < hits.length; j++ ) {
	    	Document document = searcher.doc(hits[j].doc);
	    	String id = document.get("id");
	    	String text = document.get("text").toString();
	    	//System.out.println("Page ID: " + id + ":\nContents: " + text);
	    	double score = calculatePlagiarismNaive(queryString, text);
	    	if(score > 0.6 && score < 1) {
	    		System.out.println(queryString);
	    		//System.out.println("SCORE: " + score);
	    	}
	    	//if(score == 1) System.out.println("ID: " + id);
	    	if(score > .5) writer.write(DataManager.convertToId(query.toString()) + " 0 " + id + " 1\n");
	    	//calculatePlagarism( queryString, text );
	    }
	    //System.out.println("number of ones: " + count);

  }
  
  
  private static double calculatePlagiarismNaive( String queryString, String content ) {
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
	  //if(score != 1) System.out.println("CONTENT: " + content + "\nQuery  : " + queryString + " \n");
	  //System.out.println( "Plagarism Naive percent score: " + score*100 + "%");
	  return score;
  }
  
}
