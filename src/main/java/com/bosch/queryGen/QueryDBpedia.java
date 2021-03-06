package com.bosch.queryGen;

import com.bosch.graphdeal.Util;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * generate DBpedia queries
 * @Author Yuxuan Shi
 * @Date 11/26/2019
 * @Time 10:10 AM
 */
public class QueryDBpedia {
    String goalFile = "C:\\kgdata\\query\\" + Util.DBPEDIA + "\\query.txt";

    List<IndexSearcher> is = null;
    List<String> queries = null;
    QueryDBpedia(){
        is = new ArrayList<>();
        queries = new ArrayList<>();
    }

    /**
     * get lucene index
     * @throws IOException
     */
    void getLuceneIndex() throws IOException {
        File rootPath = new File(Util.getLuceneRoot(Util.DBPEDIA));
        File[] files = rootPath.listFiles(); //a directory
        for (File file : files) {
            Directory dir = FSDirectory.open(Paths.get(file.toString()));
            DirectoryReader iReader = DirectoryReader.open(dir);
            IndexSearcher iSearcher = new IndexSearcher(iReader);
            is.add(iSearcher);
        }
    }

    void getOriginQuery() throws FileNotFoundException {
        Scanner sc = new Scanner(
                new File("C:\\kgdata\\query\\"
                        + Util.DBPEDIA + "\\dbpedia.queries-v2_stopped.txt"));
        while (sc.hasNext()){
            String line = sc.nextLine();
            int loc = line.indexOf('\t');
            String query = line.substring(loc + 1).trim().replaceAll(",","");
            queries.add(query.replaceAll(" +", " "));
        }
        sc.close();
    }

    void generateVaildQuery() throws IOException, ParseException {
        PrintWriter pw = new PrintWriter(goalFile);
        for (String que : queries){
            StringBuilder sb = new StringBuilder();
            List<Query> queryList = new ArrayList<>();
            String[] keywords = que.split(" ");

            int cnt = 0;
            //generate all keywords related
            for (String st : keywords) {
                QueryParser qp = new QueryParser("label", new StandardAnalyzer());
                Query query = qp.parse(st);
                if (!query.toString().equals("")) { //words can be hit
                    queryList.add(query);
                    sb.append(';');
                    sb.append(st);
                    cnt++;
                }
            }
            if (cnt <= 2) {
                continue;
            }
            //true if any index hits all keywords
            boolean hit = false;
            for (IndexSearcher iSearcher : is) {
                //true if iSearcher hits all keywords
                boolean isHit = true;
                for (Query query : queryList) {
                    TopDocs hits = iSearcher.search(query, 3);
                    if (hits.totalHits == 0) {
                        isHit = false;
                        break;
                    }
                }
                if (isHit) {    //iSearcher is such an index
                    hit = true;
                    break;
                }
            }

            if (hit) {
                pw.println(sb.substring(1));
            }
        }
        pw.close();
    }

    void writeIncrease() throws IOException {
        List<List<String>> queries = new ArrayList<>();
        Scanner sc = new Scanner(new File(goalFile));
        while (sc.hasNext()) {
            String line = sc.nextLine();
            int len = line.split(";").length;
            while (queries.size() <= len) {
                queries.add(new ArrayList<>());
            }
            queries.get(len).add(line);
        }
        sc.close();
        PrintWriter pw = new PrintWriter(goalFile);
        for (List<String> list : queries) {
            for (String query : list) {
                pw.println(query);
            }
        }
        pw.close();
    }
    /**
     * filter queries
     */
    void filterQuery() throws IOException {
        getLuceneIndex();
        getOriginQuery();
        try {
            generateVaildQuery();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        writeIncrease();
    }

    public static void main(String[] args) throws IOException {
        QueryDBpedia qd = new QueryDBpedia();
        qd.filterQuery();
    }
}
