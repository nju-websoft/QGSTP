import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.bosch.banksII.BiSearchNodeBase;
import com.bosch.banksII.GraphBanksII;
import com.bosch.dpbf.DPBFNodeBase;
import com.bosch.dpbf.GraphDPBF;
import com.bosch.graphdeal.*;
import com.bosch.qgstp.GraphOneStar;
import com.bosch.qgstp.OneStar;
import com.bosch.qgstp.OneStarBase;
import com.bosch.qgstp.PruneControl;

import java.io.*;
import java.util.*;

/**
 * @Author Yuxuan Shi
 * @Date 12/11/2019
 * @Time 4:45 PM
 */
public class TestAllGivenNode {
    List<List<String>> words = new ArrayList<>();;

    void inputquery(String fileName){
        try {
            double cnt = 0;
            Scanner input1 = new Scanner(new File(fileName));
            while (input1.hasNext()) {
                String line = input1.nextLine();
                String[] newwords = line.split(";");
                List<String> newquery = new ArrayList<>(Arrays.asList(newwords));
                words.add(newquery);
                cnt += newquery.size();
            }
            System.out.println(words.size() + " " + cnt / words.size());
            input1.close();
        }
        catch (FileNotFoundException e) {
            System.out.println(fileName + " not found");
        }
    }

    void outputans(String st, String fileName, List<QueryInfo> queryInfos) throws IOException {
        PrintWriter fop = new PrintWriter(
                new File(Util.getAnsTxt(st, fileName)));

        double tTime = 0, tWeight = 0, tSal = 0D, tCoh = 0D;
        for (QueryInfo queryInfo : queryInfos) {
            queryInfo.calcMid();
            fop.println(queryInfo.queryString());
            fop.println("time: "+ queryInfo.getTime() + " ms");
            fop.println("weight: "+ queryInfo.getScore() + " " + queryInfo.getSal() + " " + queryInfo.getCoh());

            tTime += queryInfo.getTime();
            tWeight += queryInfo.getScore();
            tSal += queryInfo.getSal();
            tCoh += queryInfo.getCoh();
        }
        tTime = tTime/queryInfos.size();
        tWeight = tWeight/queryInfos.size();
        tSal = tSal / queryInfos.size();
        tCoh = tCoh / queryInfos.size();
        fop.println("avg time: "+ tTime  + " ms");
        fop.println("avg weight: "+ tWeight + " " + tSal + " " + tCoh);
        fop.close();
        //storeJson(st, fileName, queryInfos);
    }

    void storeJson(String st, String fileName, List<QueryInfo> queryInfos) throws IOException {
        PrintWriter fop = new PrintWriter(
                new File(Util.getAnsJs(st, fileName)));
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(AnsTree.class, "nodes", "edges", "score");
        for (QueryInfo queryinfo : queryInfos)
            fop.println(JSON.toJSONString(queryinfo.getAns(), filter));
        fop.close();
    }

    void search(SearchBase sb, GraphBase gb, int testTime, int iTime, String graph, String alg){
        try {
            //delete the file if they already exist
            File fileTxt = new File(Util.getAnsTxt(graph, alg));
            if (fileTxt.exists()) {
                fileTxt.delete();
            }
            File fileJs = new File(Util.getAnsJs(graph, alg));
            if (fileJs.exists()) {
                fileJs.delete();
            }

            long startTime, endTime;
            List<QueryInfo> infos = new ArrayList<>();
            for (List<String> word : words)
                infos.add(new QueryInfo(word, alg));
            for (int tt = 0; tt < testTime; tt++) {
                //record the avg time and weight
                double tTime = 0D, tWeight = 0D, tSal = 0D, tCoh = 0D;
                for (int i = 0; i < words.size(); i++) {
                    //for (int i = 179; i < words.size(); i++) {
                    QueryInfo info = infos.get(i);

                    //repeat if it costs smaller than the repeat time
                    if (info.getFirstTime() < Util.REPIME) {
                        startTime = System.currentTimeMillis();
                        int itCnt = 0;
                        for (int j = 0; j < iTime; j++) {
                            itCnt++;
                            //ignore the exception of search
                            try {
                                sb.search(gb, words.get(i));
                            }catch (Exception ignored) {
                            }

                            if (!sb.isRepeatFlag()) {
                                break;
                            }
                        }
                        endTime = System.currentTimeMillis();
                        info.addTime((double) (endTime - startTime) / itCnt);
                        //store the answer tree if it is null
                        if (info.getAns() == null)
                            info.setAns(sb.getAnsTree());
                        if (info.getAns() == null) {
                            System.out.println(tt + " " + i + " No answer!" +
                                    " " + (double) (endTime - startTime) / itCnt);
                        } else {
                            System.out.println(tt + " " + i + " " + info.getAns().getScore() +
                                    " " + info.getSal() + " " + info.getCoh() +
                                    " " + (double) (endTime - startTime) / itCnt);
                        }
                    }
                    if (tt == 0) {
                        PrintWriter foptxt = new PrintWriter(
                                new FileWriter(Util.getAnsTxt(graph, alg), true));
                        foptxt.println(info.queryString());
                        foptxt.println("time: " + info.getFirstTime() + " ms");
                        foptxt.println("weight: " + info.getScore() + " " + info.getSal() + " " + info.getCoh());
                        foptxt.close();

                        tTime += info.getFirstTime();
                        tWeight += info.getScore();
                        tSal += info.getSal();
                        tCoh += info.getCoh();

                        //record the json
                        PrintWriter fop = new PrintWriter(
                                new FileWriter(Util.getAnsJs(graph, alg), true));
                        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(AnsTree.class, "nodes", "edges", "score");
                        if (info.getAns() != null) {
                            fop.println(JSON.toJSONString(info.getAns(), filter));
                        } else {
                            fop.println();
                        }
                        fop.close();
                    }
                }

                if (tt == 0) {
                    PrintWriter foptxt = new PrintWriter(
                            new FileWriter(Util.getAnsTxt(graph, alg), true));
                    tTime = tTime / infos.size();
                    tWeight = tWeight / infos.size();
                    tSal = tSal / infos.size();
                    tCoh = tCoh / infos.size();
                    foptxt.println("avg time: " + tTime + " ms");
                    foptxt.println("avg weight: " + tWeight + " " + tSal + " " + tCoh);
                    foptxt.close();
                }
            }
            outputans(graph, alg, infos);
        } catch (Exception e) {
        }
    }

    void dealWithBanks(String st) throws IOException {
        search(BiSearchNodeBase.getInstance(), GraphBanksII.getInstance(), 3, 3, st, "Banks-IIBase");
        BiSearchNodeBase.closeInstance();
        GraphBanksII.closeInstance();
    }

    void dealWithDPBF(String st){
        search(DPBFNodeBase.getInstance(), GraphDPBF.getInstance(), 3, 3, st, "DPBFBase");
        DPBFNodeBase.closeInstance();
        GraphDPBF.closeInstance();
    }

    void dealWithEO(String st) {
        double []als = new double[]{0.1, 0.5, 0.9};
        for (double al : als) {
            Util.setAlpha(al);
            search(OneStar.getInstance(), GraphOneStar.getInstance(), 3, 3, st, "EO");
        }
        OneStar.closeInstance();
        GraphOneStar.closeInstance();
    }

    void dealWithQO(String st) {
        double []als = new double[]{0.1, 0.5, 0.9};
        for (double al : als) {
            Util.setAlpha(al);
            search(OneStarBase.getInstance(), GraphOneStar.getInstance(), 3, 3, st, "QO");
        }
        OneStarBase.closeInstance();
        GraphOneStar.closeInstance();
    }

    /**
     * QO without limitation of time
     * @param st graph name
     */
    void dealWithQOUnlimited(String st) {
        Util.setCheckTimeout(false);
        double []als = new double[]{0.1, 0.5, 0.9};
        for (double al : als) {
            Util.setAlpha(al);
            search(OneStarBase.getInstance(), GraphOneStar.getInstance(), 1, 1, st, "QOUNL");
        }
        OneStarBase.closeInstance();
        GraphOneStar.closeInstance();
        Util.setCheckTimeout(true);
    }

    /**
     * EO without pruning small n
     * @param st graph name
     */
    void dealWithEOPSn(String st) {
        double []als = new double[]{0.1, 0.5, 0.9};
        OneStar.getInstance().setPrune(new PruneControl(0b0111));
        for (double al : als) {
            Util.setAlpha(al);
            search(OneStar.getInstance(), GraphOneStar.getInstance(), 3, 3, st, "EOPSn");
        }
        OneStar.closeInstance();
        GraphOneStar.closeInstance();
    }

    /**
     * EO without pruning large n
     * @param st graph name
     */
    void dealWithEOPLn(String st) {
        //double []als = new double[]{0.1, 0.5, 0.9};
        double []als = new double[]{0.9};
        OneStar.getInstance().setPrune(new PruneControl(0b1011));
        for (double al : als) {
            Util.setAlpha(al);
            search(OneStar.getInstance(), GraphOneStar.getInstance(), 3, 3, st, "EOPLn");
        }
        OneStar.closeInstance();
        GraphOneStar.closeInstance();
    }

    /**
     * EO without pruning small n
     * @param st graph name
     */
    void dealWithEOPLm(String st) {
        double []als = new double[]{0.1, 0.5, 0.9};
        OneStar.getInstance().setPrune(new PruneControl(0b1101));
        for (double al : als) {
            Util.setAlpha(al);
            search(OneStar.getInstance(), GraphOneStar.getInstance(), 3, 3, st, "EOPLm");
        }
        OneStar.closeInstance();
        GraphOneStar.closeInstance();
    }

    /**
     * EO without root vertex ranking
     * @param st graph name
     */
    void dealWithEORVR(String st) {
        double []als = new double[]{0.1, 0.5, 0.9};
        OneStar.getInstance().setPrune(new PruneControl(0b1110));
        for (double al : als) {
            Util.setAlpha(al);
            search(OneStar.getInstance(), GraphOneStar.getInstance(), 3, 3, st, "EORVR");
        }
        OneStar.closeInstance();
        GraphOneStar.closeInstance();
    }

    void testDeal() throws IOException {
        Properties pps = Util.getInitPPS();
        String st = pps.get("GRAPH_NAME").toString();
        String queryfile = Util.DATA_DIR + "\\query\\" + st + "\\query.txt";
        inputquery(queryfile);
        Util.setUSERDF2VEC(false);

        try{
            //dealWithBanks(st);
            //dealWithDPBF(st);
            dealWithEO(st);
            //dealWithQO(st);
            //dealWithQOUnlimited(st);

            //dealWithEOPSn(st);
            //dealWithEORVR(st);
            //dealWithEOPLn(st);
            //dealWithEOPLm(st);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            Runtime.getRuntime().gc();
        }
        finally {
        }

    }

    public static void main(String[] args) throws IOException {
        TestAllGivenNode t1 = new TestAllGivenNode();
        t1.testDeal();
    }
}

