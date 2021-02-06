package com.bosch.queryGen;

import com.bosch.banksII.GraphBanksII;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * @Author Yuxuan Shi
 * @Date 12/11/2019
 * @Time 4:58 PM
 */
public class QueryMondial {
    static String pp = "F:\\data\\iswc\\query\\mondial\\origin";
    static String mg = "F:\\data\\iswc\\query\\mondial\\query.txt";
    void fileMerge(String path, String mergeFile) throws IOException {
        GraphBanksII g1 = GraphBanksII.getInstance();
        List<List<String>> words = new ArrayList<>();
        File tryFile = new File(path);
        for (File file : Objects.requireNonNull(tryFile.listFiles())){
            System.out.println(file.toString());
            Scanner sc = new Scanner(file);
            if (sc.hasNext()){
                String line = sc.nextLine();
                line = line.substring(line.indexOf('#') + 1).trim();
                //build base list
                List<String> base = new ArrayList<>();
                for (String st : line.split(" ")){
                    if (st.equals("")) {
                        continue;
                    }
                    if (g1.containQueryWord(st)) {
                        base.add(st);
                    }
                }
                words.add(base);
                /*while (sc.hasNext()){
                    line = sc.nextLine();
                    if (line.indexOf('#')== -1) continue;
                    line = line.substring(line.indexOf('#') + 1).trim();
                    if (g1.containQueryWord(line)){
                        List<String> word = new ArrayList<>(base);
                        word.add(line);
                        System.out.println(word);
                        words.add(word);
                    }
                }*/
            }
            sc.close();
        }
        PrintWriter pw = new PrintWriter(mergeFile);
        for (List<String> word : words){
            if (word.size() <= 1) {
                continue;
            }
            for(int i = 0; i < word.size() - 1; i++) {
                pw.print(word.get(i) + ";");
            }
            pw.println(word.get(word.size() - 1));
        }
        pw.close();
        int cnt = 0;
        for (List<String> word : words) {
            cnt += word.size();
        }
        System.out.println(((double)cnt)/words.size());
    }

    void test() throws IOException {
        fileMerge(pp, mg);
    }

    public static void main(String[] args) throws IOException{
        QueryMondial m1 = new QueryMondial();
        m1.test();

    }
}