package lucene;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This class will use the LSH class to create list of hash values for every doc
 * 
 * @author Bobby
 *
 */
public class UseLSHOnPosIndex{
	public static void main(String []args) {
		
		//Code to get the argument string 
	    String query = "";
	    for (int i=0; i<args.length; i ++) {
	        query += args[i] + " ";
	    }
	    if ( query.equals("")) {
	    	query = getQueryDocument();
	    }
		try {
			String indexPath = "./src/main/java/positionalIndex";
			HashMap<String, String> idsWithContent = new HashMap<String, String>();
			
			HashMap<String, HashSet<Integer>> allMinHashes = new HashMap<String, HashSet<Integer>>();
			//Create the minHashes to be used
			 MinHash [] hashes = new MinHash[10];
			 for(int i = 0; i < hashes.length; i++) {
				 hashes[i] = new MinHash();
				 //System.out.println("MINHASH: " + hashes[i].toString());
			 }
			 Directory dir = FSDirectory.open(Paths.get(indexPath));
			 IndexReader reader = DirectoryReader.open(dir);
			 //loop through all docs and construct their min hashes
			 System.out.println("Creating shingles and generating min-hashes for all documents...");
			 System.out.println("Num Docs: " + reader.maxDoc());
			 for (int i=0; i<reader.maxDoc(); i++) {
				 Document doc = reader.document(i);
				 String id = reader.document(i).get("id");
				 String docText = doc.get("text");
				 //System.out.println("ID: " + id);
				 //System.out.println("DOCTEXT: " + docText);
				 idsWithContent.put(id, docText);
				 //This is how you get the hashset of doc mins
				 HashSet<Integer> docMins = LSH.minHash(LSH.createShingles(docText), hashes);
				 
				 //need to add this so all hashsets are in a lsit
				 allMinHashes.put(doc.get("id"), docMins);
				 //allMinHashes.add(doc.get("id"), LSH.minHash(LSH.createShingles(docText), hashes));
			 }
			 allMinHashes.put(getQueryId(), LSH.minHash(LSH.createShingles(getQueryDocument()), hashes));
			 System.out.println("All min-hashes have been created!");
			 System.out.println("Getting all docs with Jaccard Coefficient Greater than .7");
			 HashMap<String, ArrayList<String>> matches = LSH.getDocIdsWithJaccardCoAtLeast(allMinHashes, (float) 0.7); 
			 
			 /**Object [] keys = matches.keySet().toArray();
			 for(Object key: keys) {
				 ArrayList<String> ids = matches.get(key.toString());
				 //System.out.print(key.toString() + "::: ");
				 for(String id: ids) {
					 //System.out.print(id.toString() + ", ");
				 }
				 //System.out.println();
			 }
		
			 }*/
			 System.out.println("Getting documents for the query:");
			 ArrayList<String> ids = matches.get(getQueryId());
			 for(String id: ids) {
				 System.out.println("Doc ID:  " + id);
				 System.out.println("Content: " + idsWithContent.get(id));
			 }
			 
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	private static String getQueryId() {
		return "queryDoc";
	}
	
	private static String getQueryDocument() {
		String str = "the icc cricket world cup is the international championship of one day international (odi) cricket."
				+ " the event is organised by the sport's governing body, the international cricket council (icc), every "
				+ "four years, with preliminary qualification rounds leading up to a finals tournament. the tournament is "
				+ "one of the world's most viewed sporting events and is considered the \"flagship event of the international "
				+ "cricket calendar\" by the icc. the first world cup was organised in england in june 1975, with the first "
				+ "odi cricket match having been played only four years earlier. however, a separate women's cricket world "
				+ "cup had been held two years before the first men's tournament, and a tournament involving multiple "
				+ "international teams had been held as early as 1912, when a triangular tournament of test matches was played"
				+ " between australia, england and south africa. the first three world cups were held in england. from the 1987 "
				+ "tournament onwards, hosting has been shared between countries under an unofficial rotation system, with "
				+ "fourteen icc members having hosted at least one match in the tournament. the finals of the world cup "
				+ "are contested by the ten full members of the icc (all of which are test-playing teams) and a number of teams "
				+ "made up from associate and affiliate members of the icc, selected via the world cricket league and a later "
				+ "qualifying tournament. a total of twenty teams have competed in the eleven editions of the tournament, with "
				+ "fourteen competing in the latest edition in 2015. australia has won the tournament five times, with the "
				+ "west indies, india (twice each), pakistan and sri lanka (once each) also having won the tournament. the "
				+ "best performance by a non-full-member team came when kenya made the semi-finals of the 2003 tournament. ";
		
		return str;
	}
}
