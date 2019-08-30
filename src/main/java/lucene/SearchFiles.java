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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

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
import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class SearchFiles {

  private SearchFiles() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    //This is a directory to the index
    String index = "../index";
    
    if ( args.length < 1) {
    	throw new IllegalArgumentException("Invalid number of arguments!");
    }
    
    //Code to get the argument string 
    String queryString = "";
    for (int i=0; i<args.length; i ++) {
    	if (i > 0) {
    		queryString = args[i] + " ";
    	}
    }
    
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    Analyzer analyzer = new StandardAnalyzer();

    searcher.setSimilarity(new BM25Similarity());
    
    QueryParser queryParser = new QueryParser("default search", analyzer);
    
    Query query = queryParser.parse(queryString);
    TopDocs searchResult = searcher.search(query,100);
    //searchResult.scoreDocs;
    ScoreDoc[] hits = searchResult.scoreDocs;
    for (ScoreDoc doc : hits) {
    	Document document = searcher.doc(doc.doc);
    	String id = document.getField("id").toString();
    	String content = document.getField("text").toString();
    	System.out.print(id + ":" + content);
    	
    }
  }
}

  