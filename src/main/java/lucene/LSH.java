package lucene;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class will be the tool that we can use to use locality sensitive hashing to find similiar documents
 * For our LSH we will be doing unigram word level shingling due to the large amount of data, may change
 * @author Bobby
 *
 */
public class LSH {
	
	private static String LSHFilePath = "./src/main/java/data/LSH.txt";
	//This value will determine how large the ngram are (how many words in a row are a gram)
	private static int nGramValue = 5;
	private static HashSet<String> stopWords = DataManager.getStopWordsFromFile("./src/main/java/data/stopWords.txt");
	
	public LSH() {
		
	}
	
	/**
	 * This mthod goes through the string given and constructs n grams, the n value given in the variable nGramValue
	 * and puts each ngram into a hashset to represent the doc, duplicate ngrams will not be processed
	 * @param docString
	 * @return
	 */
	public static HashSet<String> createShingles(String docString) {
		String [] words = convertStringToArrayOfWords(docString);
		//Construct each n-gram
		HashSet<String> nGramInDoc = new HashSet<String>();
		for(int i = 0; i < words.length; i++) {
			//this is the each ngram, at the end it's adde to the hashset
			StringBuilder build = new StringBuilder();
			int j = i + 1;
			build.append(words[i]);
			//Loop through n-1 words after j because j is included
			while(j < (nGramValue + i) ) {
				if(j >= words.length) {
					return nGramInDoc;
				}
				build.append(" " + words[j]);
				j++;
			}
			nGramInDoc.add(build.toString());
			//System.out.println(build.toString());
		}
		return nGramInDoc;
	}
	
	/**
	 * This method calculates the minHash of a given doc
	 * @param d document hashset
	 * @param hashes array of n hash functions
	 * 
	 * @return returns HashSet of n length of hashvalues using the provided hash functions, 
	 * MUST be the same for every doc to get proper results
	 */
	public static HashSet<Integer> minHash(HashSet<String> d, MinHash [] hashes) {
		HashSet<Integer> minHashSet = new HashSet<Integer>();
		//int numHashes = 0;
		for(MinHash hash: hashes) {
			//System.out.println("Num Hashes: " + numHashes);
			if(hash == null) return minHashSet;
			minHashSet.add(hash.getMinHash(d));
			//numHashes++;
		}
		
		
		return minHashSet;
	}
	
	/**
	 * This takes to HashSets that represent ngrams in a doc and calculates the jaccard co effecient
	 * can also be used on 2 hash sets of integers values generated from min hash
	 * @param d1 document 1's ngrams
	 * @param d2 document 2's ngrams
	 */
	public static float calcJaccardCo(HashSet<Integer> d1, HashSet<Integer> d2) {
		float s1 = d1.size();
		float s2 = d2.size();
		if(s1 == 0 || s2 == 0) return (float) 0.0;
		float inCommon = 0;
		//Check which length is smaller
		if(d1.size() < d2.size()) {
			inCommon = getNumCommonGrams(d1, d2);
		}
		else {
			inCommon = getNumCommonGrams(d2, d1);
		}
		//System.out.println("Numerator  : " + inCommon);
		//System.out.println("Denominator: " + (s1 + s2 - inCommon));
		
		float jac = inCommon/(s1 + s2 - inCommon);
		//System.out.println("\t jac co: " + jac);
		return jac;
	}
	
	/**
	 * This method loops through the smaller set, checks how many the second set has in common and returns the number
	 * @param smaller the doc with the smaller number of n-grams
	 * @param longer the doc with the larger number of n-grams
	 */
	private static float getNumCommonGrams(HashSet<Integer> smaller, HashSet<Integer> longer) {
		int inCommon = 0;
		for(Integer gram : smaller) {
			//System.out.println(gram);
			if(longer.contains(gram)) inCommon++;
		}
		//System.out.println("\tIn common: " + inCommon);
		return (float) inCommon;
	}
	
	//These 2 methods do the exact same thing as the on above but with HashSets of string
	public static float calcJaccardCoStrings(HashSet<String> d1, HashSet<String> d2) {
		float s1 = d1.size();
		float s2 = d2.size();
		if(s1 == 0 || s2 == 0) return (float) 0.0;
		float inCommon = 0;
		//Check which length is smaller
		if(d1.size() < d2.size()) {
			inCommon = getNumCommonGramsStrings(d1, d2);
		}
		else {
			inCommon = getNumCommonGramsStrings(d2, d1);
		}
		//System.out.println("Numerator  : " + inCommon);
		//System.out.println("Denominator: " + (s1 + s2 - inCommon));
		return inCommon/(s1 + s2 - inCommon);
	}
	
	/**
	 * This method loops through the smaller set, checks how many the second set has in common and returns the number
	 * @param smaller the doc with the smaller number of n-grams
	 * @param longer the doc with the larger number of n-grams
	 */
	private static float getNumCommonGramsStrings(HashSet<String> smaller, HashSet<String> longer) {
		int inCommon = 0;
		for(String gram : smaller) {
			if(longer.contains(gram)) inCommon++;
		}
		//System.out.println("In common: " + inCommon);
		return (float) inCommon;
	}
	
	/**
	 * Returns a hashmap of array lists that is a docID -> a list of rlated docIDS
	 * @param allHashes
	 * @param threshold
	 */
	public static HashMap<String, ArrayList<String>> getDocIdsWithJaccardCoAtLeast(HashMap<String, HashSet<Integer>> allHashes, float threshold ) {
		String [] keys = allHashes.keySet().toArray(new String[allHashes.size()]);
		/**for (int i = 0; i < keys.length; i++) { 
			for (int j = i + 1 ; j < keys.length; j++) { 
				if (keys[i].equals(keys[j])) {
					System.out.println("DUPLICATE KEY FOUND!!!!!");
				} 
		
			}
		}**/
		//System.out.println();
		//This is an hashmap of docIDS -> matches to other docIDs based on threshold provided
		HashMap<String, ArrayList<String>> matches = new HashMap<String, ArrayList<String>>();
		//int numDocsRelated = 0;
		for(int i = 0; i < keys.length - 1; i++) {
			for(int j = i + 1; j < keys.length; j++) {
				float jac = LSH.calcJaccardCo(allHashes.get(keys[i]), allHashes.get(keys[j]));
				if(jac >= threshold && !keys[i].equals(keys[j])) {
					//System.out.println("D1: " + keys[i] + " D2: " + keys[j] + " Jaccard Coefficient: " + jac);
					/**System.out.print(keys[i] + " grams: ");
					for(Integer gram: allHashes.get(keys[i])) {
						System.out.print( gram + " ");
					}
					System.out.print("\n" + keys[j] + " grams: " );
					for(Integer gram: allHashes.get(keys[j])) {
						System.out.print( gram + " ");
					}*/
					//The next 2 ifs are to put the related docs in a hashmap
					if(matches.get(keys[i]) == null){
						ArrayList<String> tmp = new ArrayList<String>();
						tmp.add(keys[j]);
						matches.put(keys[i], tmp);
					}
					else matches.get(keys[i]).add(keys[j]);
					
					if(matches.get(keys[j]) == null){
						ArrayList<String> tmp = new ArrayList<String>();
						tmp.add(keys[i]);
						matches.put(keys[j], tmp);
					}
					else matches.get(keys[j]).add(keys[i]);
				}
			}
		}
		System.out.println("Number of relations: " + matches.size());
		return matches;
	}
	
	public static String[] convertStringToArrayOfWords(String splitMe) {
		//first replace all removes all punctuation, second replaces all multiple spaces with 1
    	String line = splitMe.replaceAll("\\p{P}", "").toLowerCase().replaceAll("\\s+", " ");
    	return line.split(" ");
	}
	public static String removePunctuationAndStopWords(String str) {
		String [] temp = convertStringToArrayOfWords(str.replaceAll("\\p{P}", "").toLowerCase().replaceAll("\\s+", " "));
		StringBuilder build = new StringBuilder();
		for(String word: temp) {
			if(!stopWords.contains(word)) {
				build.append(word + " ");
			}
		}
		return build.toString();
	}
	
	public static String getFilePath() {
		return LSHFilePath;
	}
	
}
