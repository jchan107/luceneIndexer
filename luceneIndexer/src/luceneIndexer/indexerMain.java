package luceneIndexer;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;


public class indexerMain {
	
	public static void main(String[] args) throws IOException, ParseException {

        Analyzer analyzer = new StandardAnalyzer();
        // Store the index in memory:
        Directory directory = new RAMDirectory();
        // To store an index on disk, use this instead:
        //String basePath = new File("").getAbsolutePath();
        //basePath = basePath + "\\indexFile";
        //Directory directory = FSDirectory.open(Paths.get(basePath));
        
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter w = new IndexWriter(directory, config);
		try
		{
			File txt = new File(args[0] + "\\\\index.txt");
			FileReader fileReader = new FileReader(txt);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			int cnt = 0;// just to check indexing on smaller number of htmls
			while ((line = bufferedReader.readLine()) != null && cnt < 5000) {
				String[] indexANDUrl = line.split("\\|");
				System.out.println(indexANDUrl[0]);
				System.out.println(indexANDUrl[1]);
				File htmlF = new File(args[0] + "\\" + indexANDUrl[0] + ".html");
				org.jsoup.nodes.Document doc = Jsoup.parse(htmlF, "UTF-8", "");
				String title = doc.getElementsByTag("title").text();
				System.out.println(title);
				String body = doc.getElementsByTag("body").text();
				System.out.println(body);
				System.out.println();
				
				Document d = getDocument(title,body,indexANDUrl[1]);
				w.addDocument(d);
				cnt++;
			}
			bufferedReader.close();
			w.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//search Index
		DirectoryReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        String[] fields = {"title", "body"};
        Map<String, Float> boosts = new HashMap<>();
        boosts.put(fields[0], 1.0f);
        boosts.put(fields[1], 0.5f);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer, boosts);
        Query query = parser.parse("School");
        // Query query = parser.parse("UCR discussion");
        // QueryParser parser = new QueryParser("content", analyzer);
        // Query query = parser.parse("(title:ucr)^1.0 (content:ucr)^0.5");
        System.out.println(query.toString());
        int topHitCount = 10;
        ScoreDoc[] hits = indexSearcher.search(query, topHitCount).scoreDocs;

        // Iterate through the results:
        for (int rank = 0; rank < hits.length; ++rank) {
            Document hitDoc = indexSearcher.doc(hits[rank].doc);
            System.out.println((rank + 1) + " (score:" + hits[rank].score + ") --> " +
                               hitDoc.get("title") + " - " + hitDoc.get("url") + " - " + hitDoc.get("body"));
            // System.out.println(indexSearcher.explain(query, hits[rank].doc));
        }
        indexReader.close();
        directory.close();
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
	
	protected static Document getDocument(String title, String body,String url) {
		Document doc = new Document();
		doc.add(new TextField("title", title,Field.Store.YES));
		doc.add(new TextField("body", body, Field.Store.YES));
		doc.add(new StringField("url", url, Field.Store.YES));
		return doc;
	}


}
