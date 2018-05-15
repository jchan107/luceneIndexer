package luceneIndexer;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;


public class indexerMain {
	
	public static void main(String[] args) {
		IndexWriter w = getIndexWriter("");
		for(int i = 0; i < 120000; ++i) {
			System.out.println(i);
			File f = new File(args[0] + i + ".html");
			try {
				w.addDocument(getDocument(f));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	static IndexWriter getIndexWriter(String dir) {
		Directory indexDir;
		try {
			indexDir = FSDirectory.open(Paths.get("builtIndex"));
			IndexWriterConfig luceneConfig = new IndexWriterConfig(
					new StandardAnalyzer());
			return(new IndexWriter(indexDir, luceneConfig));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	protected static Document getDocument(File f) {
		Document doc = new Document();
		try {
			doc.add(new TextField("contents", new FileReader(f)));
			doc.add(new StringField("filename", f.getName(), Field.Store.YES));
			doc.add(new StringField("fullpath", f.getCanonicalPath(), Field.Store.YES));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}


}
