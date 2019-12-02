package lucene;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class RankWriter {
	
	  private RankWriter() {}

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
	    
	    ArrayList<String> queries = new ArrayList<String>();
	    System.out.println("Application Starting Basic Plagarism... \n");

	    queries = DataManager.get25Queries();
	    System.out.println("number of queries: " + queries.size());
	    BufferedWriter writer = new BufferedWriter(new FileWriter("./src/main/java/analysis/rankings.qrels"));
	    try {
	    	for(int i = 0;i < queries.size(); i++) {
	    		runSearch(queries.get(i), indexPath, matches, lsh, writer);
	    	}
	    } catch(Exception e) {
	    	System.out.println("Query failed! " + e.getMessage());
	    }
	    writer.close();
	    
	  }
	  
	  private static void runSearch(String queryString, String indexPath, HashMap<String, ArrayList<String>> matches, boolean lsh, BufferedWriter writer) throws Exception {
	     queryString = queryString.replaceAll("\\. ", "\\.  " );  
		  Directory dir = FSDirectory.open(Paths.get(indexPath));
		    IndexReader reader = DirectoryReader.open(dir);
		    IndexSearcher searcher = new IndexSearcher(reader);
		    //Similarity simul = CustomSimilarity.getSimilarity("UL", (double) uniqueTerms.size(), (double) termCount );
		    
		    //searcher.setSimilarity(new ClassicSimilarity());
		    searcher.setSimilarity(new BM25Similarity());
		    //This sets up the query
		    Analyzer analyzer = new StandardAnalyzer();
		    QueryParser queryParser = new QueryParser("text", analyzer);
		    //Query query = queryParser.parse(QueryParser.escape(queryString));
		    Query query = queryParser.parse(QueryParser.escape(queryString));
		    
		    
		    //This initiates the search and returns top 10
		    //System.out.println("STARTING RETREVAl: " + query.toString());
		    TopDocs searchResult = searcher.search(query,10);
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
		    //System.out.println( "unsorted" + Pscores );
		    Collections.sort(Pscores, Comparator.comparing(p -> -p.fst));
		    //System.out.println(Pscores);
		    
		    int j =0;
		    String qId = DataManager.convertToId(queryString);
		    /**
		    for ( Pair<Double, String> score : Pscores ) {
		    	writer.write( qId + " Q0 " + score.snd + " " + j + " " + score.fst + " Team11-BM25_Simularity\n" );
		    	j++;
		    }
		    */
		    //qrel file production below 
		    
		    for ( Pair<Double, String> score : Pscores ) {
	    		int rel = 0;
		    	if ( score.fst > 0.1 ) {
		    		rel = 1;
		    		writer.write( qId + " 0 " + score.snd + " " + rel +"\n" );
		    	}
		    	j++;
		    }
		    
		    
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
