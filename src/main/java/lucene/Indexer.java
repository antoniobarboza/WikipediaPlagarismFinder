/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lucene;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar_v2.Data.Paragraph;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

/** Index all text files under a directory.
 * It is currently hard-coded and does not take any input
 * 
 * @author Bobby Chisholm
 * 
 */
public class Indexer {
  
  private Indexer() {}

  /** Index all text files under a directory. */
  public static void main(String[] args) {
    String indexPath = "./src/main/java/index";
    String docsPath = "./src/main/java/lucene/test200-train/train.pages.cbor-paragraphs.cbor";
    
    File input = new File(docsPath);
    
    try {
        //try to open the index to be written to
    	Directory dir = FSDirectory.open(Paths.get(indexPath));
    	Analyzer analyzer = new StandardAnalyzer();
    	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);

    	//Creates a new index folder, deletes any old data
    	indexWriterConfig.setOpenMode(OpenMode.CREATE);

    	IndexWriter indexWriter = new IndexWriter(dir, indexWriterConfig);
    	
    	indexDoc(indexWriter, input);
    	indexWriter.close();
    } catch(Exception e) {
    	e.printStackTrace();
    }

  }

  /**
   * Creates Lucene Documents from an input file with paragraphs, then indexes them
   * *This currently only supports using 1 file*
   * 
   * @param writer Writes to the given index
   * @param file file to be parsed into Lucene Documents
   * 
   * @throws IOException If there is a low-level I/O error
   */
  static void indexDoc(final IndexWriter writer, File file) throws Exception {
	  //System.out.println("PATH: " + file.getAbsolutePath());
	  FileInputStream fileStream = new FileInputStream(file);
	  //convert all data into paragraphs
	  Iterable<Paragraph> paragraphs = null;
	  try {
	  paragraphs = DeserializeData.iterableParagraphs(fileStream);
	  } catch(Exception e) {
		  //conversion failed
		  throw e;
	  }
	  int commit = 0;
	  System.out.println("Indexing documents...");
	  for(Paragraph paragraph : paragraphs) {
          if (commit == 50) {
              writer.commit();
              commit = 0;
          }
		  //System.out.println("PARAGRAPH : " + paragraph.getTextOnly());
		  Document doc = new Document();
		  doc.add(new StringField("id", paragraph.getParaId(), Field.Store.YES));   //Correct this needs to be a stringfield
		  doc.add(new TextField("text", paragraph.getTextOnly(), Field.Store.YES)); //Correct this needs to be Textfield
		  writer.addDocument(doc);
		  commit++;
	  }
	  writer.commit();
	  System.out.println("All documents indexed!");
  }

  
}
