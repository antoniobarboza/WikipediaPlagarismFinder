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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNodeImpl;
import org.apache.lucene.queryparser.flexible.standard.builders.MultiPhraseQueryNodeBuilder;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

/** Simple command-line based search demo. */
public class PositionalSearcher {
	
  private PositionalSearcher() {}

  /** Simple command-line based search demo. 
 * @throws Exception */
  public static void main(String[] args) throws Exception{
	//This is a directory to the index
	    //String indexPath = "./src/main/java/positionalIndex";
	    String indexPath = "./src/main/java/index";
	    String query = "The ICC Cricket World Cup is the international championship of One Day International (ODI) cricket.";
	    //String query = "cricket is a sport";
	    HashMap<String, ArrayList<String>> matches = null;
	    //true if the locality sensitive hashing should be used false other wise
	    boolean lsh = true;
	    
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
	    //We are using a phraseQuery here because it checks for the terms in consecutive order within slop
	    //PhraseQuery doc;
	    runSearch(query, indexPath, matches, lsh);
    
  }
  
  /**
   * The query string to be used
   * @param queryString
   * @param indexPath
   * @param matches
   */
  private static void runSearch(String queryString, String indexPath, HashMap<String, ArrayList<String>> matches, boolean lsh){
	  try {
	    
		Directory dir = FSDirectory.open(Paths.get(indexPath));
	    IndexReader reader = DirectoryReader.open(dir);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    searcher.setSimilarity(new BM25Similarity());
	    
	    //This sets up the query
	    Analyzer analyzer = new StandardAnalyzer();
	    QueryParser queryParser = new QueryParser("text", analyzer);
	    //QueryParser queryParser = new QueryParser(Version.LUCENE_7_2_0, .Term_Vector_Position, analyzer); 
	    //ComplexPhraseQueryParser queryParser = new ComplexPhraseQueryParser("text", analyzer);
	    //MultiPhraseQuery mpq = (MultiPhraseQuery) queryParser.parse("\"Testing\"");
	    MultiPhraseQuery query;
	    /**Query query;
	    Builder build = new BooleanQuery.Builder();
	    BooleanClause clause = new BooleanClause(query, null);
	    
	    build.add(clause);*/
	    
	    
	    MultiPhraseQuery.Builder pqbuild = new MultiPhraseQuery.Builder();
	    pqbuild.setSlop(2);
	    String [] words= LSH.convertStringToArrayOfWords(LSH.removePunctuationAndStopWords(queryString));
	    Term [] terms = new Term[words.length];
	    for(int i = 0; i < words.length; i++) {
	    	terms[i] = new Term("text", words[i]);
	    	//System.out.println(words[i]);
	    }
	    
	    for(int i = 0; i < terms.length; i++) {
	    	pqbuild.add(terms[i]);
	    }
	    pqbuild.setSlop(10);
	    query = (MultiPhraseQuery) pqbuild.build();
	    
	    //Query query = queryParser.parse(QueryParser.escape(queryString));
	    //MultiPhraseQuery mpq;
	    //String escString = LSH.removePunctuationAndStopWords(queryString);
	    //System.out.println("ESC STRING : " + escString);
	    //BytesRef bytes = new BytesRef(escString);
	    
	    //Query query = queryParser.createPhraseQuery("text", queryString);
	    
	    //This initiates the search and returns top 10
	    //System.out.println("STARTING RETREVAl: " + query.toString());
	    
	    
	    TopDocs searchResult = searcher.search(query,50);
	    ScoreDoc[] hits = searchResult.scoreDocs;
	    
	    //System.out.println("Results found: " + searchResult.totalHits);
	    
	    //If there are no results
	    if (hits.length == 0) {
	        System.out.println("No result found for: " + queryString);   	
	        System.out.println("NUM DOCS: " + reader.getDocCount("text"));
	    }
	    else {
	    	System.out.println("Results for query: " + queryString);
	    }
	    
	    for (int j=0; j < hits.length; j++ ) {
	    	Document document = searcher.doc(hits[j].doc);
	    	String id = document.get("id");
	    	String text = document.get("text").toString();
	    	System.out.println("Page ID: " + id + ":\nContents: " + text);
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
	    }
	  }
	  catch(Exception e) {
		  System.out.println("Query: " + queryString + " Failed!");
		  e.printStackTrace();
	  }
  }
}

  