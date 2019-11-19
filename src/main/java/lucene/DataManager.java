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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
public class DataManager {
	private static String eof = "EOF";

    private static void usage() {
        System.out.println("Command line parameters: (header|pages|outlines|paragraphs) FILE");
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
  	 
  	 for(String pageCat: pagesCategories) {
  		 for(String wanted: categoriesWanted) {
  			 if(pageCat.toLowerCase().contains(wanted)) {
  				 try {
  					 //This will show the category that returned this pid as in a category
  					writer.write(wanted + ":  " + pid + "\n");
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
   public static void writePageIdsInCategoryToFile(ArrayList<String> categoriesWanted, Iterable<Page> pages, BufferedWriter writer) {
	   for(Page p: pages) {
		   writeToFileIfInCategory(categoriesWanted, p, writer);
	   }
	   try {
		writer.write(eof);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
   
   /**
    * Tis method will return all of the pageIds that were a part of the categories we gathered from writePageIdsInCategoryToFile
    * @param filePath
    * @return
 * @throws Exception 
    */
   public static HashSet<Page> getPageIdsFromFile(String filePath) throws Exception{
	    BufferedReader reader = new BufferedReader(new FileReader(filePath));
	    HashSet<String> ids = new HashSet<String>();
   	 	String line = reader.readLine();
   	 	line = line.replaceAll("\\s+", " ");
   	 	String[] arrayLine = line.split(" ");
   	 	while(line != null && !arrayLine[0].equals(eof)) {
   	 		
   	 		//Get next line
   	 		line = reader.readLine();
	    	line = line.replaceAll("\\s+", " ");
	    	arrayLine = line.split(" ");
   	 	}
   	 	reader.close();
	  
	  return null;
   }
}
