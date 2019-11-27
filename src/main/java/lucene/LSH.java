package lucene;

import java.io.BufferedReader;
import java.util.HashSet;

/**
 * This class will be the tool that we can use to use locality sensitive hashing to find similiar documents
 * For our LSH we will be doing bigram word level shingling due to the large amount of data, may change
 * @author Bobby
 *
 */
public class LSH {
	
	private static  String LSHFilePath = "./src/main/java/data/LSH.txt";
	//This value will determine how large the ngram are (how many words in a row are a gram)
	private static int nGramValue = 2;
	
	
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
		}
		return nGramInDoc;
	}
	
	/**
	 * This takes to HashSets that represent ngrams in a doc and calculates the jaccard co effecient
	 * @param d1 document 1's ngrams
	 * @param d2 document 2's ngrams
	 */
	public static float calcJaccardCo(HashSet<String> d1, HashSet<String> d2) {
		float s1 = d1.size();
		float s2 = d2.size();
		float inCommon = 0;
		//Check which length is smaller
		if(d1.size() < d2.size()) {
			inCommon = getNumCommonGrams(d1, d2);
		}
		else {
			inCommon = getNumCommonGrams(d2, d1);
		}
		
		return inCommon/(s1 + s2 - inCommon);
	}
	
	/**
	 * This method loops through the smaller set, checks how many the second set has in common and returns the number
	 * @param smaller the doc with the smaller number of n-grams
	 * @param longer the doc with the larger number of n-grams
	 */
	private static float getNumCommonGrams(HashSet<String> smaller, HashSet<String> longer) {
		int inCommon = 0;
		for(String gram : smaller) {
			if(longer.contains(gram)) inCommon++;
		}
		return (float) inCommon;
	}
	
	private static String[] convertStringToArrayOfWords(String splitMe) {
    	String line = splitMe.replaceAll("\\s+", " ");
    	return line.split(" ");
	}
	
	public static String getFilePath() {
		return LSHFilePath;
	}
}
