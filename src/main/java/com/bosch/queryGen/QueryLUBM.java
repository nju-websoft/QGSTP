package com.bosch.queryGen;

import com.bosch.database.MysqlReader;
import com.bosch.graphdeal.Util;
import org.apache.jena.atlas.io.IO;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author Yuxuan Shi
 * @date 2020/5/21
 */
public class QueryLUBM {

    private String goalGraphName;

    private String goalFile;
    private String mapFile;

    public QueryLUBM(String goalGraphName){
        this.goalGraphName = goalGraphName;
        File fp = new File(Util.DATA_DIR + "query\\" + goalGraphName);
        if (!fp.exists()) {
            fp.mkdirs();
        }
        goalFile = Util.DATA_DIR + "query\\" + goalGraphName + "\\query.txt";

        mapFile = Util.DATA_DIR + "query\\" + goalGraphName + "\\querymap.txt";

    }


    public void generate(){
        try {
            //get nodeNum
            int nodeNum = 0;
            MysqlReader mr = new MysqlReader(goalGraphName);
            mr.dbInit("nodeID");

            while (mr.readNodeID() != null) {
                nodeNum++;
            }
            System.out.println(nodeNum);
            mr.close();

            PrintWriter pw1 = new PrintWriter(new File(goalFile));
            PrintWriter pw2 = new PrintWriter(new File(mapFile));
            Random r1 = new Random();
            List<Integer> avgSize = Arrays.asList(100, 10, 100, 1000, 100);
            List<Integer> keyNumbs = Arrays.asList(2, 4, 4, 4, 6);
            int cnt = 0;
            for (int id = 0; id < avgSize.size(); id++) {
                int keyNum = keyNumbs.get(id);
                int avg = avgSize.get(id);
                //query number
                for (int j = 0; j < 50; j++) {
                    //generate the query
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < keyNum; k++) {
                        sb.append((cnt + k));
                        sb.append(';');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    pw1.println(sb.toString());

                    //generate the map
                    for (int k = 0; k < keyNum; k++) {
                        sb = new StringBuilder();
                        Set<Integer> si = new TreeSet<>();
                        while (si.size() < avg) {
                            si.add(r1.nextInt(nodeNum));
                        }
                        sb.append((cnt + k));
                        sb.append(':');
                        for (int it : si) {
                            sb.append(it);
                            sb.append(',');
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        pw2.println(sb.toString());
                    }
                    cnt = cnt + keyNum;
                }
            }
            pw1.close();
            pw2.close();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args){
        QueryLUBM ql = new QueryLUBM(Util.LUBM100K);
        ql.generate();
    }
}
