package com.bosch.qgstp;

import com.bosch.graphdeal.ConnectedGraph;
import com.bosch.graphdeal.GraphBase;
import com.bosch.graphdeal.HopVDE;
import com.bosch.graphdeal.Util;

import java.util.*;

/**
 * @Author Yuxuan Shi
 * @Date 10/30/2019
 * @Time 4:49 PM
 */
public class GraphOneStar extends GraphBase {
    boolean bitFlag;	//ture if bitList is not empty
    final int MaxKeyWord = 32;	//max number of keywords
    List<BitSet>bitList;
    int []distI;	//number of edges from root to v
    double []distD;	//distance between root and v
    boolean []visited;
    int []father;
    int [][]disAll; //numberof edges from each node to keyword node set

    @Override
    protected void graphInit(){
        bitList = new ArrayList<BitSet>();
        for (int v = 0; v < biggestNodeNum; v++) {
            bitList.add(new BitSet(MaxKeyWord));
        }
        distI = new int[biggestNodeNum];
        Arrays.fill(distI, -1);
        distD = new double[biggestNodeNum];
        Arrays.fill(distD, -1D);
        visited = new boolean[biggestNodeNum];
        Arrays.fill(visited, false);
        father = new int[biggestNodeNum];
        Arrays.fill(father, -1);
        bitFlag = false;
    }

    private static GraphOneStar instance = null;

    public static GraphOneStar getInstance(){
        if (instance == null) {
            if (!Util.BOSCHKG) {
                Properties pps = Util.getInitPPS();
                if (!"false".equals(pps.get("INIT"))) {
                    instance = new GraphOneStar(pps.get("DATABASE").toString(),
                            pps.get("GRAPH_NAME").toString());
                }
            } else {
                instance = new GraphOneStar("C:\\kgdata\\inallrdf\\triples",
                        "C:\\kgdata\\inallrdf\\atype", "boschkg");
            }
        }
        return instance;
    }

    public static void closeInstance() {
        instance = null;
    }

    public GraphOneStar(String database, String graphName){
        readGraph(database, graphName);
    }
    public GraphOneStar(String fileOrDir, String typeFileOrDir, String graphName){
        readGraph(fileOrDir, typeFileOrDir, graphName);
    }

    //to check if backtrack is correct
    boolean check(){
        for (int i : distI) {
            if (i != -1) return false;
        }
        for (double i : distD) {
            if (i >= 0) return false;
        }
        for (boolean i : visited) {
            if (i) return false;
        }
        for (int i : father) {
            if (i != -1) return false;
        }
        return true;
    }

    //clear bitList
    void bitClear(ConnectedGraph c1) {
        bitFlag = false;
        if (keywordList == null) {
            return;
        }
        for (String it : keywordList) {
            for (Integer keyNode : c1.getKeynode(it)) {
                bitList.get(keyNode).clear();
            }
        }
        //keywordList = null;
    }

    void bitSet(ConnectedGraph c1, List<String> keywordList0){
        if (bitFlag) bitClear(c1);
        if (keywordList0.size() > MaxKeyWord) {
            System.out.println("Too many keywords to deal with,at most "+ MaxKeyWord);
            return;
        }

        bitFlag = true;

        for (int i = 0; i < queryKeyNum; i++) {
            for (Integer keyNode : c1.getKeynode(keywordList.get(i))) {
                bitList.get(keyNode).set(i);
            }
        }

        disAll = new int[c1.getNodeNum()][queryKeyNum];
        for (int i = 0; i < c1.getNodeNum(); i++) {
            Arrays.fill(disAll[i], -1);
        }

        //get distance from each keyword
        for (int i = 0; i < queryKeyNum; i++) {
            bfs(c1, i);
        }
    }

    void bitLogSet(ConnectedGraph c1, List<String> keywordList0){
        if (bitFlag) {
            bitClear(c1);
        }
        if (keywordList0.size() > MaxKeyWord) {
            System.out.println("Too many keywords to deal with,at most "+ MaxKeyWord);
            return;
        }
        //givenQueryWord(keywordList0);
        bitFlag = true;

        for (int i = 0; i < queryKeyNum; i++) {
            for (Integer keyNode : c1.getKeynode(keywordList.get(i))) {
                bitList.get(keyNode).set(i);
            }
        }
    }

    //bfs from a set of nodes
    void bfs(ConnectedGraph c1, int keyID){
        Queue<Integer> q = new LinkedList<>();
        for (int v : c1.getKeynode(keywordList.get(keyID))){
            if (disAll[v][keyID] == -1){
                disAll[v][keyID] = 0;
                q.add(v);
            }
        }
        while (!q.isEmpty()){
            int u = q.poll();
            for (HopVDE it : c1.getEdges(u)){
                int v = it.v;
                if (disAll[v][keyID] == -1){ //not visited
                    disAll[v][keyID] = disAll[u][keyID] + 1;
                    q.add(v);
                }
            }
        }
    }

    //bfs from root, the shortest distance from root to each keyword
    boolean bfs(ConnectedGraph c1, int r, int[]s) {
        BitSet bitAll = new BitSet(keywordList.size());
        bitAll.set(0, bitAll.length());
        List<Integer> q = new ArrayList<Integer>();
        q.add(r);
        int start = 0;
        distI[r] = 0;
        int coverdBitCnt = 0;
        while (coverdBitCnt < queryKeyNum && start < q.size()) {
            int u = q.get(start);
            //get all newly covered bit
            BitSet bitNewCovered = (BitSet) bitList.get(u).clone();
            bitNewCovered.xor(bitAll); //different bit location
            bitNewCovered.and(bitList.get(u)); //different location with bitList's 1
            if (!bitNewCovered.isEmpty()){
                for (int i = bitNewCovered.nextSetBit(0); i >= 0; i = bitNewCovered.nextSetBit(i+1)) {
                    s[i] = distI[u];
                }
                //update newly coverd bit
                bitAll.or(bitNewCovered);
                coverdBitCnt = bitAll.cardinality();
            }
            for (HopVDE it: c1.getEdges(u)) {
                int v = it.v;
                if (distI[v] == -1) {
                    distI[v] = distI[u] + 1;
                    q.add(v);
                }
            }
            start++;
        }
        for (int v : q) {
            distI[v] = -1;
        }
        q.clear();

        //if (!check()) System.out.println("dfs recover failed!");
        for (int value : s) {
            if (value < 0) {
                return false;
            }
        }
        return true;
    }

    //dijkstra from root
    List<List<Map.Entry<Integer,Integer>>> dijkstra(ConnectedGraph c1, int r, int[]sk, int k, double[] op, double alpha) {
        double beta = 1 - alpha;
        //double opt = op[0];
        BitSet bitAll = new BitSet(keywordList.size());
        bitAll.set(0, bitAll.length());
        Queue<Map.Entry<Integer, Double>> q = new PriorityQueue<>((s1, s2) -> {
            if (s1.getValue()>s2.getValue()) {
                return 1;
            } else {
                return -1;
            }
        });
        List<Integer> visitList = new ArrayList<>();
        q.add(new AbstractMap.SimpleEntry<Integer, Double>(r, 0D));
        distD[r] = 0D;	//root node's weight is not counted here
        distI[r] = 0;	//edge numbers
        father[r] = r;
        double ans = c1.getNodeWeight(r)*alpha;
        List<List<Map.Entry<Integer,Integer>>> edges = new ArrayList<>();
        for (int i = 0; i < queryKeyNum; i++) edges.add(new ArrayList<>());
        int coverdBitCnt = 0;
        double totalT = 0D;
        while (coverdBitCnt < queryKeyNum && !q.isEmpty()) {
            totalT += Math.log(q.size());
            int u = q.poll().getKey();
            if (visited[u]) continue;
            visited[u] = true;
            visitList.add(u);
            //get all newly covered bit
            BitSet bitNewCovered = (BitSet) bitList.get(u).clone();
            bitNewCovered.xor(bitAll); //different bit location
            bitNewCovered.and(bitList.get(u)); //different location where bitList is 1
            if (!bitNewCovered.isEmpty()){
                //get the path from r to u in reverse order
                List<Map.Entry<Integer,Integer>> path = new ArrayList<>();
                int uEdge = u;
                while (father[uEdge] != uEdge) {
                    path.add(new AbstractMap.SimpleEntry<>(father[uEdge], uEdge));
                    uEdge = father[uEdge];
                }
                for (int i = bitNewCovered.nextSetBit(0); i >= 0; i = bitNewCovered.nextSetBit(i+1)) {
                    sk[i] = distI[u];
                    ans += distD[u];
                    edges.set(i, path);
                }
                //update newly coverd bit
                bitAll.or(bitNewCovered);
                coverdBitCnt = bitAll.cardinality();
            }
            if (ans > op[0]) {
                break;
            }
            for (HopVDE it : c1.getEdges(u)) {
                int v = it.v;
                if (visited[v]) {
                    continue;
                }
                double dis = c1.sde(r,v) * k / 2;
                if (distD[v] < 0 || distD[v] > distD[u] + alpha * c1.getNodeWeight(v) + beta * dis) {
                    distD[v] = distD[u] + alpha * c1.getNodeWeight(v) + beta * dis;
                    distI[v] = distI[u] + 1;
                    father[v] = u;
                    q.add(new AbstractMap.SimpleEntry<>(v, distD[v]));
                }
            }
        }

        //nodes in q may be pruned
        while (!q.isEmpty()) {
            int v = q.poll().getKey();
            visitList.add(v);
        }
        for (int v : visitList) {
            distD[v] = -1D;
            distI[v] = -1;
            visited[v] = false;
            father[v] = -1;
        }
        op[0] = ans;
        return edges;
    }
}
