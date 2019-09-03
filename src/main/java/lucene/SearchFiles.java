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
import java.nio.file.Paths;


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
public class SearchFiles {

  private SearchFiles() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    //This is a directory to the index
    String indexPath = "./src/main/java/index";
    if ( args.length < 1) {
    	//throw an illegal if not given a query
    	throw new IllegalArgumentException("Invalid number of arguments!");
    }
    
    //Code to get the argument string 
    String queryString = "";
    for (int i=0; i<args.length; i ++) {
        queryString += args[i] + " ";
    }
    //queryString = "power nap benefits";
    //queryString = "whale vocalization production of sound";
    //queryString = "pokemon puzzle league";
    
    System.out.println("QueryString : " + queryString);
    
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
    TopDocs searchResult = searcher.search(query,10);
    ScoreDoc[] hits = searchResult.scoreDocs;
    
    //System.out.println("Results found: " + searchResult.totalHits);
    
    //TESTIING
    if (hits.length == 0) {
        System.out.println("no hits...");   	
    }
    
    for (int j=0; j < hits.length; j++ ) {
    	Document document = searcher.doc(hits[j].doc);
    	String id = document.getField("id").toString();
    	String text = document.getField("text").toString();
    	System.out.print(id + ":" + text);
    }
  }
}

  