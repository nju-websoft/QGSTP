package com.bosch.qgstp;

import com.bosch.graphdeal.*;
import java.util.*;


/**
 * @Author Yuxuan Shi
 * @Date 10/30/2019
 * @Time 4:49 PM
 */
public class OneStar extends SearchBase {
    public enum OneStarMode {G, L, ExpB, Exp, Poly}

    protected GraphOneStar gos = null;
    protected double opt;
    protected AnsTreeOneStar ansTree;
    protected int keywordNum;

    protected PruneControl prune;

    private static OneStar instance = null;

    public static OneStar getInstance() {
        if (instance == null) {
            Properties pps = Util.getInitPPS();
            if (!"false".equals(pps.get("INIT"))) {
                instance = new OneStar();
            }
        }
        return instance;
    }

    public static void closeInstance() {
        instance = null;
    }

    @Override
    public AnsTree getAnsTree() {
        return ansTree;
    }

    @Override
    public JsonTree JsonGenerate() {
        JsonTree js = new JsonTree();
        js.buildTree(ansTree, "OneStar");
        return js;
    }

    public void setPrune(PruneControl prune) {
        this.prune = prune;
    }

    public OneStar() {
        ansTree = null;
        opt = Double.MAX_VALUE;
        prune = new PruneControl();
    }

    @Override
    public void search(GraphBase og1, List<String> keywordList) throws Exception {
        ansTree = null;
        startTime = System.currentTimeMillis();
        setRepeatFlag();
        opt = Double.MAX_VALUE;
        gos = (GraphOneStar) og1;
        gos.givenQueryWord(keywordList);
        for (ConnectedGraph c1 : gos.cg) {
            if (!c1.keyContains(keywordList)) {
                continue;
            }
            if (logSearch(c1, keywordList)) {
                //localMergePolySearch(keywordList);
            }
            assert c1.checkTreeCover(ansTree, keywordList);
        }
        if (ansTree == null) {
            throw new Exception("find no answer");
        }
        //ansTree.printTree();
    }

    @Override
    public void search(GraphBase og1, List<String> keywordList, Map<String, List<Integer>> mp) throws Exception {
        ansTree = null;
        startTime = System.currentTimeMillis();
        setRepeatFlag();
        opt = Double.MAX_VALUE;
        gos = (GraphOneStar) og1;
        gos.givenQueryWord(keywordList, mp);
        for (ConnectedGraph c1 : gos.cg) {
            if (!c1.keyContains(keywordList)) {
                continue;
            }
            if (logSearch(c1, keywordList)) {
                //localMergePolySearch(keywordList);
            }
            assert c1.checkTreeCover(ansTree, keywordList);
        }
        if (ansTree == null) {
            throw new Exception("find no answer");
        }
    }

    /**
     * search OneStar/OneStarL
     *
     * @param og1         graph class
     * @param keywordList keyword list
     * @param osm         variant of algorithm(OneStar/OneStarL)
     */
    public void search(GraphOneStar og1, List<String> keywordList, OneStarMode osm) throws Exception {
        ansTree = null;
        startTime = System.currentTimeMillis();
        setRepeatFlag();
        opt = Double.MAX_VALUE;
        gos = og1;
        gos.givenQueryWord(keywordList);
        for (ConnectedGraph c1 : gos.cg) {
            if (!c1.keyContains(keywordList)) {
                continue;
            }
            switch (osm) {
                case G: {
                    goodSearch(c1, keywordList);
                    break;
                }
                case L: {
                    logSearch(c1, keywordList);
                    break;
                }
                default:
                    break;
            }
        }
        if (ansTree == null) {
            throw new Exception("find no answer");
        }
        //ansTree.printTree();
    }

    private void setOpt(ConnectedGraph c1, int r, double newOpt) {
        setOpt(newOpt);
    }

    private void setOpt(double newOpt) {
        opt = newOpt;
    }


    /**
     * get all candidate roots, in increasing order of
     * the total distance from root to each keyword node set.
     *
     * @param c1         connected subgraph
     * @param candiRoots where we store all candidate roots
     */
    void candiChoose(ConnectedGraph c1, List<Map.Entry<Integer, Integer>> candiRoots) {
        // disable root vertex ranking
        if (!prune.isRootVertexRanking()) {
            for (int i = 0; i < c1.getNodeNum(); i++) {
                candiRoots.add(new AbstractMap.SimpleEntry<>(0, i));
            }
            return;
        }
        // enable root vertex ranking
        for (int i = 0; i < c1.getNodeNum(); i++) {
            boolean flagUnreach = false;
            boolean flagKeynode = false;
            int sums = 0;
            for (int j = 0; j < gos.disAll[i].length; j++) {
                switch (gos.disAll[i][j]) {
                    case -1:
                        flagUnreach = true;
                        break;
                    case 0:
                        flagKeynode = true;
                        break;
                    default:
                        sums += gos.disAll[i][j];
                }
            }
            if (flagUnreach) {
                continue;
            }
            if (!flagKeynode && c1.getEdges(i).size() == 1) {
                continue;
            }
            candiRoots.add(new AbstractMap.SimpleEntry<>(sums, i));
        }
        candiRoots.sort(Map.Entry.comparingByKey());
    }

    //start from all nodes
    protected boolean goodSearch(ConnectedGraph c1, List<String> keywordList) {
        gos.bitSet(c1, keywordList);
        keywordNum = gos.getKeywordNum();
        List<Map.Entry<Integer, Integer>> candidateRoots = new ArrayList<>();
        candiChoose(c1, candidateRoots);

        int[] s; //edge number of shortest path from root to each keyword with unit weight

        boolean findFlag = false;
        for (int iter = 0; iter < candidateRoots.size(); iter++) {
            int r = candidateRoots.get(iter).getValue();
            s = gos.disAll[r];
            if (scorePFind(c1, s, r)) {
                findFlag = true;
            }
            if (isTimeOut()) {
                break;
            }
        }
        gos.bitClear(c1);
        return findFlag;
    }

    /**
     * code of OneStarL,log means the O(log k) loss of approximation ratio
     *
     * @param c1          connected subgraph
     * @param keywordList list of keywords
     * @return true if find a better tree
     */
    protected boolean logSearch(ConnectedGraph c1, List<String> keywordList) {
        gos.bitSet(c1, keywordList);
        List<Map.Entry<Integer, Integer>> candidates = new ArrayList<>();
        candiChoose(c1, candidates);
        gos.bitClear(c1);

        gos.bitLogSet(c1, keywordList);
        keywordNum = gos.getKeywordNum();

        int[] s = new int[keywordNum]; //edge number of shortest path from root to each keyword with unit weight
        List<Integer> candidateRoots = new ArrayList<>();

        //N10
        if (prune.isRootVertexRanking()) {
            for (int i = 0; i < 10; i++) {
                candidateRoots.add(candidates.get(i).getValue());
            }
        }
        int minLoc = 0;
        for (int i = 0; i < keywordNum; i++) {
            if (c1.getKeynode(keywordList.get(i)).size()
                    < c1.getKeynode(keywordList.get(minLoc)).size()) {
                minLoc = i;
            }
        }
        candidateRoots.addAll(c1.getKeynode(keywordList.get(minLoc)));

        if (!prune.isRootVertexRanking()) {
            for (int i = 0; i < 10; i++) {
                candidateRoots.add(candidates.get(i).getValue());
            }
        }

        boolean findFlag = false;
        for (int iter = 0; iter < candidateRoots.size(); iter++) {
            int r = candidateRoots.get(iter);
            gos.bfs(c1, r, s);
            if (scorePFind(c1, s, r)) {
                findFlag = true;
            }
            if (isTimeOut()) {
                break;
            }
        }
        gos.bitClear(c1);
        return findFlag;
    }

    /**
     * find the optimal tree with least score', given the root
     * implementation of FindRPS
     *
     * @param c1 connected subgraph
     * @param s  s[i] means the number of fewest edges from root to keyword node set i
     * @param r  root
     * @return true if successfully find a better tree
     */
    private boolean scorePFind(ConnectedGraph c1, int[] s, int r) {
        boolean findFlag = false;
        int sums = 1;   //smallest 1-star's node number

        for (int i = 0; i < keywordNum; i++) {
            sums += s[i];
        }
        if (!prune.isPruneSmallN()) {
            sums = 1;
        }
        //edge number of shortest path from root to each keyword
        int[] sk = new int[keywordNum];
        for (int k = sums; k <= c1.getNodeNum() * keywordNum; k++) {
            if (isTimeOut()) {
                break;
            }
            Arrays.fill(sk, -1);
            double[] op = new double[1];
            op[0] = opt;
            //get the ans path of 1-star
            List<List<Map.Entry<Integer, Integer>>> ansPaths = gos.dijkstra(c1, r, sk, k, op, Util.ALPHA);
            if (prune.isPruneLargeN()) {
                //further k are pruned
                if (Util.EqualBigger(op[0], opt)) {
                    break;
                }
            }
            int sumsk = 1, maxsk = 0;
            for (int i = 0; i < keywordNum; i++) {
                if (maxsk < sk[i]) {
                    maxsk = sk[i];
                }
                sumsk += sk[i];
            }
            if (sumsk < k) {
                //already considered when k = sumsk
                continue;
            }
            if (!prune.isPruneLargeM()) {
                maxsk = Math.min(k - 1, c1.getNodeNum() - 1);
            }

            //a better candidate
            if (sumsk == k) {
                AnsTreeOneStar newAns = new AnsTreeOneStar(c1, r);
                for (List<Map.Entry<Integer, Integer>> it : ansPaths) {
                    newAns.addPath(it);
                }
                newAns.calcScore(Util.ALPHA);
                if (newAns.getScore() > op[0] * 2) {
                    System.out.println("Nope! wrong ans, r: " + r);
                }
                //newAns.printTree(gos);
                if (ansTree == null || newAns.getScore() < ansTree.getScore()) {
                    ansTree = newAns;
                    findFlag = true;
                }
                setOpt(c1, r, op[0]);
                continue;
            }

            //line 4-5
            //dp to get dist with given edge number
            //dp[i][u] means the minimum cost of paths from r to u using i nodes, r excluded
            double[][] dp = new double[maxsk + 1][c1.getNodeNum()];
            int[][] father = new int[maxsk + 1][c1.getNodeNum()];
            for (int i = 0; i <= maxsk; i++) {
                for (int u = 0; u < c1.getNodeNum(); u++) {
                    dp[i][u] = -1D;
                    father[i][u] = -1;
                }
            }

            //line 6-8
            dp[0][r] = 0D;
            father[0][r] = r;
            for (int i = 1; i <= maxsk; i++) {
                for (int u = 0; u < c1.getNodeNum(); u++) {
                    for (HopVDE it : c1.getEdges(u)) {
                        int v = it.v;
                        if (dp[i - 1][v] >= 0 && (dp[i][u] < 0 || dp[i - 1][v] < dp[i][u])) {
                            dp[i][u] = dp[i - 1][v];
                            father[i][u] = v;
                        }
                    }
                    //only connected nodes are updated
                    if (dp[i][u] >= 0 && u != r) {
                        dp[i][u] += Util.ALPHA * c1.getNodeWeight(u)
                                + Util.BETA * k * c1.sde(r, u) / 2;
                    }
                }
            }

            //line 9-11
            //get d[i][j]
            //d[i][j] means the minimum cost of paths from r to keywrod set j using i nodes, r excluded
            double[][] d = new double[maxsk + 1][keywordNum];
            int[][] hit = new int[maxsk + 1][keywordNum];
            for (int i = 0; i <= maxsk; i++) {
                for (int j = 0; j < keywordNum; j++) {
                    d[i][j] = -1D;
                    hit[i][j] = -1;
                    for (int v : c1.getKeynode(gos.keywordList.get(j))) {
                        if (dp[i][v] < 0) {
                            continue;
                        }
                        if (d[i][j] < 0 || d[i][j] > dp[i][v]) {
                            d[i][j] = dp[i][v];
                            hit[i][j] = v;
                        }
                    }
                }
            }

            //line 12-13
            //get f[i]
            double[] f = new double[k];    //f[i] means the cost of minimum 1-star using i nodes, r excluded
            //fNode : [j][i] When using j nodes, covering first i keywords, nodes used to cover i-th keyword
            int[][] fNode = new int[k][keywordNum];

            for (int i = 0; i < Math.min(k, maxsk + 1); i++) {
                f[i] = d[i][0];
                fNode[i][0] = i;
            }
            for (int i = maxsk + 1; i < k; i++) {
                f[i] = -1;
            }

            // line 14-16
            //to cover first i keywords
            for (int i = 1; i < keywordNum; i++) {
                //using j nodes in all
                for (int j = k - 1; j >= 0; j--) {
                    //the first time to cover the i-th keyword
                    boolean firstTime = true;
                    //use t nodes to cover i-th keyword
                    for (int t = s[i]; t <= Math.min(sk[i], j); t++) {
                        //f[j-t] is not valid or d[t][i] is not valid
                        if (f[j - t] < 0 || d[t][i] < 0) {
                            continue;
                        }
                        if (f[j] < 0 || d[t][i] + f[j - t] < f[j] || firstTime) {
                            f[j] = d[t][i] + f[j - t];
                            fNode[j][i] = t;
                            firstTime = false;
                        }
                    }
                    if (firstTime) {
                        f[j] = -1D;
                    }
                }
            }

            if (f[k - 1] < 0) {
                continue;
            }
            //note root r has not been counted
            f[k - 1] += Util.ALPHA * c1.getNodeWeight(r);

            //line 17-19
            //try generate new ans tree
            if (Util.EqualBigger(opt, f[k - 1])) {
                AnsTreeOneStar newAns = new AnsTreeOneStar(c1, r);
                setOpt(c1, r, f[k - 1]);
                int vertexNum = k - 1;
                for (int i = keywordNum - 1; i >= 0; i--) {
                    //build the path from r to keyword i
                    int pathNum = fNode[vertexNum][i];    //pathNum is the number of edges of the path from r to keyword i
                    vertexNum = vertexNum - pathNum;    //remaining vertex number
                    int u = hit[pathNum][i];    //u is the end point of path from r to keyword i
                    List<Map.Entry<Integer, Integer>> dpPath = new ArrayList<>();    //in reverse order
                    while (pathNum > 0) {
                        dpPath.add(new AbstractMap.SimpleEntry<>(father[pathNum][u], u));    //not in reverse order
                        u = father[pathNum][u];
                        pathNum--;
                    }

                    //add path to tree
                    newAns.addPath(dpPath);
                }
                //try to substitute ans tree
                newAns.calcScore(Util.ALPHA);
                if (ansTree == null || newAns.getScore() < ansTree.getScore()) {
                    ansTree = newAns;
                    findFlag = true;
                }
            }
        }
        return findFlag;
    }
}
