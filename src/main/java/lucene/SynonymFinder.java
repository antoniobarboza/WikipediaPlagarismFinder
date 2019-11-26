package lucene;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import edu.smu.tspell.wordnet.*;


public class SynonymFinder {
	public static WordNetDatabase _database;
	public SynonymFinder() {
		File f=new File("./src/main/java/data/WordNet-3.0/dict");
        System.setProperty("wordnet.database.dir", f.toString());
        //setting path for the WordNet Directory
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        _database = database;
        
	}
	public static HashSet<String> findSyn( String wordForm ) {
		//This will return a hashset of words that are syn but will return null if there are none!
		
		Synset[] synsets = _database.getSynsets(wordForm);

        if (synsets.length > 0){
           //ArrayList<String> al = new ArrayList<String>();
           // add elements to al, including duplicates
           HashSet<String> hs = new HashSet<String>();
           for (int i = 0; i < synsets.length; i++){
         	  if ( i == 3 ) {
         		  break;
         	  }
              String[] wordForms = synsets[i].getWordForms();
              for (int j = 0; j < wordForms.length; j++)
              {
             	 hs.add(wordForms[j]);
              }
           }
           //System.out.println(hs.size());
           //Iterator<String> it = hs.iterator();
           
           //String tmp;
           
           return hs;
           /**
           while( it.hasNext() ) {
         	  tmp = it.next();
         	  System.out.println(tmp);
           }            
           */  
        }
        else
        {
     	   System.err.println("No synsets exist that contain the word form '" + wordForm + "'");
     	   return null;
        }
	}
	public static void main(String[] args) {

	}
}
