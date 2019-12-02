package lucene;

import java.util.HashSet;

/**
 * This class creates a MinHash function
 * Each instance will have different values of a and b
 * hash function is taken from: https://mccormickml.com/2015/06/12/minhash-tutorial-with-python-code/
 * 
 * a and b are randomly chosen integers less than max value of x, c is a prime number slightly larger than x
 * template: f(x) = (ax + b) % c
 * I will be hard coding x and c for this
 * @author Bobby
 *
 */
public class MinHash {
	private long a;
	private int b;
	private int c = 2147483647; //10007;
	private int x = 2147483646; //10000;
	
	public MinHash() {
		//generates a random number between 0 and x
		a = (int)(Math.random() * x);
		b = (int)(Math.random() * x);
		//System.out.println("A: " + a + " B: " + b);
	}
	
	/**
	 * This method calculates the hash value of the given string using the input generated in the constructor
	 * @param s
	 * @return
	 */
	private int getHash(String s) {
		//System.out.println("S HASH CODE: " + s.hashCode());
		//System.out.println("x = " + s.hashCode());
		if(s.isEmpty()) return -1;
		int tmp = (int) ((a * (s.hashCode()) + b) % c);
		//if(tmp == 2147483647 ) System.out.println("MAXINT: " + s);
		return tmp;
	}
	
	/**
	 * This method goes through each word in the doc and finds the min hash value
	 * @param words
	 * @return
	 */
	public int getMinHash(HashSet<String> words) {
		/**for(String word: words) {
			System.out.print(word + " ");
		}
		System.out.println();*/
		
		int min = Integer.MAX_VALUE;
		//int numWords = 0;
		for(String word: words) {
			//System.out.println("Hashing " + word );
			int tmp = getHash(word);
			//System.out.println("HashValue: " + tmp);
			if( tmp < min) min = tmp;
			//numWords++;
		}
		//System.out.println("Num WORDS: " + numWords);
		return min;
	}
	
	public String toString()
	{
		return "f(x) = (" + a + "x + " + b + ") % " + c;
	}
	//This method is not needed just hardcoding max value for strings
	/**
	 * This method generates the nearest prime number to the number given
	 * @param x
	 * @return
	 */
	/**public int getNextPrime(int x) {
		int prime = x;
		boolean isPrime = false;
		while(isPrime == false) {
			isPrime = true;
			for(int i = prime-1; i > 2; i--) {
				for(int j = 2; j < prime; j++) {
					if (i % j == 0) {
						isPrime = false;
						break;
					}
				}
				if(!isPrime) break;
			}
			if(!isPrime) prime++;
		}
		
		return prime;
	}
	*/

}
