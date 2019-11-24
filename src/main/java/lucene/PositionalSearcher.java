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
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/** Simple command-line based search demo. */
public class PositionalSearcher {
	
  private PositionalSearcher() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args){
	//This is a directory to the index
	    String indexPath = "./src/main/java/positionalIndex";
	    String query = "cricket";
	    //We are using a phraseQuery here because it checks for the terms in consecutive order within slop
	    //PhraseQuery doc;
	    runSearch(query, indexPath);
    
  }
  
  private static void runSearch(String queryString, String indexPath){
	  try {
	    Directory dir = FSDirectory.open(Paths.get(indexPath));
	    IndexReader reader = DirectoryReader.open(dir);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    searcher.setSimilarity(new BM25Similarity());
	    
	    //This sets up the query
	    Analyzer analyzer = new StandardAnalyzer();
	    QueryParser queryParser = new QueryParser("text", analyzer);
	    Query query = queryParser.parse(QueryParser.escape(queryString));
	    
	    
	    BytesRef bytes = new BytesRef(QueryParser.escape(queryString));
	    //PhraseQuery query = new PhraseQuery(10, "text", bytes);
	    
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
	    }
	  }
	  catch(Exception e) {
		  System.out.println("Query: " + queryString + " Failed!");
		  e.printStackTrace();
	  }
  }
}

  