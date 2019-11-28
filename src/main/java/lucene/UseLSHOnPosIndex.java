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
 * NEED TO REMOVE ESCAPE CHARACTERS
 * @author Bobby
 *
 */
public class UseLSHOnPosIndex{
	public static void main(String []args) {
		try {
			String indexPath = "./src/main/java/positionalIndex";
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
				 String docText = doc.get("text");
				 //This is how you get the hashset of doc mins
				 HashSet<Integer> docMins = LSH.minHash(LSH.createShingles(docText), hashes);
				 
				 //need to add this so all hashsets are in a lsit
				 allMinHashes.put(doc.get("id"), docMins);
				 //allMinHashes.add(doc.get("id"), LSH.minHash(LSH.createShingles(docText), hashes));
			 }
			 System.out.println("All min-hashes have been created!");
			 System.out.println("Printing all docs with Jaccard Coefficient Greater than .7");
			 HashMap<String, ArrayList<String>> matches = LSH.getDocIdsWithJaccardCoAtLeast(allMinHashes, (float) 0.7); 
			 Object [] keys = matches.keySet().toArray();
			 for(Object key: keys) {
				 ArrayList<String> ids = matches.get(key.toString());
				 System.out.print(key.toString() + "::: ");
				 for(String id: ids) {
					 System.out.print(id.toString() + ", ");
				 }
				 System.out.println();
			 }
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}
}
