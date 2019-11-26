package lucene;
//package edu.unh.cs;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Page;
import edu.unh.cs.treccar_v2.Data.PageMetadata;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.nio.file.Paths;


public class DataManager {
	private static String eof = "EOF";
	private static ArrayList<String> wantedStrings = initializeList();
	private static String idsPath = "./src/main/java/data/pageId.txt";
    private static void usage() {
        System.out.println("Command line parameters: (header|pages|outlines|paragraphs|cat) FILE");
        System.exit(-1);
    }
    
    public static void main(String[] args) throws Exception {
        System.setProperty("file.encoding", "UTF-8");

        if (args.length<2)
            usage();
        String mode = args[0];
        if (mode.equals("header")) {
            final String pagesFile = args[1];
            final FileInputStream fileInputStream = new FileInputStream(new File(pagesFile));
            System.out.println(DeserializeData.getTrecCarHeader(fileInputStream));
            System.out.println();
        }
        else if ( mode.equals("cat")) {
        	System.out.println("Cat mode active... Starting..");
        	final String pagesFile = args[1];
        	final FileInputStream fileInputStream = new FileInputStream(new File(pagesFile));
        	final String outFile = idsPath;
        	
        	writePageIdsInCategoryToFile( wantedStrings, DeserializeData.iterableAnnotations(fileInputStream), outFile );
        	System.out.println("Cat mode active... Done..");
        }
        else if (mode.equals("pages")) {
            final String pagesFile = args[1];
            final FileInputStream fileInputStream = new FileInputStream(new File(pagesFile));
            for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {
                System.out.println(page);
                System.out.println();
            }
        } else if (mode.equals("outlines")) {
            final String pagesFile = args[1];
            final FileInputStream fileInputStream3 = new FileInputStream(new File(pagesFile));
            for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream3)) {
                for (List<Data.Section> sectionPath : page.flatSectionPaths()){
                    System.out.println(Data.sectionPathId(page.getPageId(), sectionPath)+"   \t "+Data.sectionPathHeadings(sectionPath));
                }
                System.out.println();
            }
        } else if (mode.equals("paragraphs")) {
            final String paragraphsFile = args[1];
            final FileInputStream fileInputStream2 = new FileInputStream(new File(paragraphsFile));
            for(Data.Paragraph p: DeserializeData.iterableParagraphs(fileInputStream2)) {
                System.out.println(p);
                System.out.println();
            }
        } else {
            usage();
        }

    }
    private static ArrayList<String> initializeList(){
    	ArrayList<String> build = new ArrayList<String>();
    	//build.add("sport");
    	//build.add("football");  //78000
    	//build.add("soccer");
    	//build.add("baseball");  //14000
    	build.add("cricket");   //10000
    	//build.add("basketball");  //10000
    	//build.add("golf");       //3000
    	//build.add("hockey");     //13000
    	//build.add("mlb");
    	//build.add("nfl");
    	//build.add("nhl");
    	//build.add("nba");
    	//build.add("olympics");
    	//build.add("boxing");
    	//build.add("mvp");
    	return build;
    	
    }
    
    public static ArrayList<String> getDefaultCategoryList() {
    	return wantedStrings;
    }
    
    public static String getIdsPath() {
    	return idsPath;
    }
    
    /**
     * This method is used to check if a single page is a part of the category we are looking for, it then writes the page's id to a file.
     * This method is only called from writePageIdInCategoryToFile
     * 
     * @param category the string that we want the metadata of the page to contain, ignores case
     * @param p the page that we want to check if it is in the category
     */
    
   private static void writeToFileIfInCategory(ArrayList<String> categoriesWanted, Page p, BufferedWriter writer) {
  	 PageMetadata meta = p.getPageMetadata();
  	 String pid = p.getPageId();
  	 ArrayList<String> pagesCategories = meta.getCategoryNames();
  	try {
		writer.write("temp:  " + pid + "\n");
  	} catch (IOException e) {
		e.printStackTrace();
  	}
  	 for(String pageCat: pagesCategories) {
  		 for(String wanted: categoriesWanted) {
  			 if(pageCat.toLowerCase().contains(wanted)) {
  				 try {
  					 //This will show the category that returned this pid as in a category
  					 // we are trying to only get the first 10000 ids becuase it takes 8 hours to index
  					writer.write(wanted + ":  " + pid + "\n");
  					//System.out.println(wanted + ":  " + pid );
  				} catch (IOException e) {
  					e.printStackTrace();
  				}
  				 return;
  			 }
  		 }
  	 }
  	 
   }
   
   /**
    * This method goes through a list of pages and checks if the pages metadata contains any one of the categories provided.
    * @param categoriesWanted list of categories we want, MUST be lowercase
    * @param pages pages to iterate through
    * @param writer the writer to the file that will store all of the page id's that we want to process
    */
   public static void writePageIdsInCategoryToFile(ArrayList<String> categoriesWanted, Iterable<Page> pages, String path) {
	   try {
		   	System.out.println("Begining to build the cat file...");
		   	Files.deleteIfExists(Paths.get(path));
	    	//Create the file to be written to
	    	File defaultRankOutputFile = new File(path);
	    	defaultRankOutputFile.createNewFile();
		   BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		   
		   int count = 0;
		   for(Page p: pages) {
			   writeToFileIfInCategory(categoriesWanted, p, writer);
		   }
		   writer.write(eof);
		   writer.close();
		   System.out.println("Done building the cat file...");
	   } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
   
   /**
    * Tis method will return all of the pageIds that were a part of the categories we gathered from writePageIdsInCategoryToFile
    * @param filePath
    * @return HashSet of page ids that we want to process, all other page ids will be skipped over
 * @throws Exception 
    */
   public static HashSet<String> getPageIdsFromFile(String filePath) throws Exception{
	    BufferedReader reader = new BufferedReader(new FileReader(filePath));
	    HashSet<String> ids = new HashSet<String>();
   	 	String line = reader.readLine();
   	 	line = line.replaceAll("\\s+", " ");
   	 	String[] arrayLine = line.split(" ");
   	 	while(line != null && !arrayLine[0].equals(eof)) {
	    	line = line.replaceAll("\\s+", " ");
	    	arrayLine = line.split(" ");
   	 		ids.add(arrayLine[1]);
   	 		//Get next line
   	 		line = reader.readLine();
   	 		
   	 	}
   	 	reader.close();
	  
	  return ids;
   }
   
}
