package lucene;

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

public class SynonymSearch {
	
	  private SynonymSearch() {}

	  /** Simple command-line based search demo. */
	  public static void main(String[] args) throws Exception {
	    //This is a directory to the index
	    String indexPath = "./src/main/java/index";
	    
	    //Code to get the argument string 
	    String queryString = "";
	    for (int i=0; i<args.length; i ++) {
	        queryString += args[i] + " ";
	    }
	    ArrayList<String> queries = new ArrayList<String>();
	    queries.add("cricket");
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
		    TopDocs searchResult = searcher.search(query,5);
		    ScoreDoc[] hits = searchResult.scoreDocs;
		    
		    //System.out.println("Results found: " + searchResult.totalHits);
		    
		    //If there are no results
		    if (hits.length == 0) {
		        System.out.println("No result found for: " + queryString);   	
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
}
