package com.bosch.banksII;

import com.bosch.graphdeal.*;

import java.util.*;


/**
 * @Author Yuxuan Shi
 * @Date 10/31/2019
 * @Time 9:52 AM
 */
public class BiSearchNodeBase extends SearchBase{
    private double [][]dist;
    private int [][]sp;
    private boolean[] visited;	//used for remove duplicate visit
    private int keyNum;
    private Queue<AnsTreeBanksIINode> ansQueue;	//max heap
    private List<AnsTreeBanksIINode> ansList;
    private AnsTreeBanksIINode bestAns;
    private int topk = 1;
    private double mu = 0.5;
    private double [][]at;
    private double []atv;
    private int []depth;
    private MaxHeap Qin, Qout;
    private GraphBanksII g1;

    private static BiSearchNodeBase instance = null;
    public static BiSearchNodeBase getInstance(){
        if (instance == null) {
            Properties pps = Util.getInitPPS();
            if (!"false".equals(pps.get("INIT"))) {
                instance = new BiSearchNodeBase();
            }
        }
        return instance;
    }

    public static void closeInstance() {
        instance = null;
    }

    @Override
    public AnsTree getAnsTree(){
        return bestAns;
    }
    @Override
    public JsonTree JsonGenerate(){
        if (bestAns == null) {
            return null;
        }
        JsonTree js = new JsonTree();
        js.buildTree(bestAns, "BanksII");
        return js;
    }

    public BiSearchNodeBase(){
        ansList = new ArrayList<>();
        ansQueue = new PriorityQueue<>((o1, o2) -> (int)(o2.getWeight()-o1.getWeight()));
    }

    void para_init(ConnectedGraph c1) {
        keyNum = g1.getKeywordNum();
        visited = new boolean[c1.getNodeNum()];
        for (int i = 0; i < c1.getNodeNum(); i++) {
            visited[i] = false;
        }

        Qin = new MaxHeap();
        Qin.heapInit(c1.getNodeNum(), keyNum);

        Qout = new MaxHeap();
        Qout.heapInit(c1.getNodeNum(), keyNum);

        dist = new double[c1.getNodeNum()][keyNum];
        for (int i = 0; i < c1.getNodeNum(); i++) {
            for (int j = 0; j < keyNum; j++) {
                dist[i][j] = -1;
            }
        }
        sp = new int[c1.getNodeNum()][keyNum];
        for (int i = 0; i < c1.getNodeNum(); i++) {
            for (int j = 0; j < keyNum; j++) {
                sp[i][j] = -1;
            }
        }

        at = new double[c1.getNodeNum()][keyNum];
        for (int i = 0; i < c1.getNodeNum(); i++) {
            for (int j = 0; j < keyNum; j++) {
                at[i][j] = 0;
            }
        }

        atv = new double[c1.getNodeNum()];
        for (int i = 0; i < c1.getNodeNum(); i++)
            atv[i] = 0;

        depth = new int[c1.getNodeNum()];
        for (int i = 0; i < c1.getNodeNum(); i++)
            depth[i] = -1;

        for (int i = 0; i < keyNum; i++) {
            List<Integer> Si=  c1.getKeynode(g1.keywordList.get(i));
            for (int it : Si) {
                at[it][i] = (1.0- c1.getNodeWeight(it)) / Si.size() * (1 - mu);
                atv[it] += at[it][i];
                dist[it][i] = c1.getNodeWeight(it);
                sp[it][i] = it;
                depth[it] = 0;
                Qin.tryHigher(it, atv[it]);
            }
        }

        //calculate incoming reverse edge weight
        g1.nodeInWeight = new ArrayList<>();
        for (int i = 0; i < c1.getNodeNum(); i++) {
            double weight = 0;
            for (HopVDE it : c1.getEdges(i)) {
                weight += 1.0;
            }
            g1.nodeInWeight.add(weight);
        }
        ansQueue.clear();
    }
    void bidir_exp_search(ConnectedGraph c1){
        para_init(c1);

        while (true) {
            if (isTimeOut()) {
                break;
            }
            //get node with highest activation
            double maxatv = -1;
            int goal = -1;
            if (!Qin.isempty()) {
                if (maxatv < Qin.topatv()) {
                    maxatv = Qin.topatv();
                    goal = 1;
                }
            }
            if (!Qout.isempty()) {
                if (maxatv < Qout.topatv()) {
                    maxatv = Qout.topatv();
                    goal = 2;
                }
            }
            if (maxatv < 0) {
                break;
            }

            switch (goal) {
                case 1:
                    extend(c1, Qin, true, Qout);
                    break;
                case 2:
                    extend(c1, Qout, false, null);
                    break;
                default:
                    System.out.println("Nope, find no node to extend!");
                    break;
            }

        }
    }

    //merge line 7-14 and 16-23 in one function
    void extend(ConnectedGraph c1, MaxHeap q1, boolean isIn, MaxHeap q2) {
        //Pop best v from Q and insert in X
        HeapNode topnode = q1.pop();
        //System.out.println("extend node : "+topnode.v + " atv: " + topnode.activation);
        //if is-Complete(u) then EMIT(u)
        if (is_complete(topnode.v)) {
            emit(c1, topnode.v);
        }
        //if depth u < d max then
        if (depth[topnode.v] < Util.DIAMETER / 2) {
            //∀sh.v ∈ outgoing[topnode.v]
            for (HopVDE sh : c1.getEdges(topnode.v)) {
                //ExploreEdge(u,v)
                exploreEdge(c1, sh.v, topnode.v, c1.getNodeWeight(sh.v));
                //if v /∈  X
                if (depth[sh.v] < 0) {
                    depth[sh.v] = depth[topnode.v] + 1;
                }
                q1.tryHigher(sh.v, atv[sh.v]);
            }
            if (!isIn) {
                return;
            }
            if (!q2.inX(topnode.v)) {
                q2.tryHigher(topnode.v, atv[topnode.v]);
            }
        }
    }

    void exploreEdge(ConnectedGraph c1, int u, int v, double w) {
        //for each keyword i
        for (int i = 0; i < keyNum; i++) {
            //if u has a better path to t i via v then
            if (dist[v][i] >= 0 && (dist[u][i] < 0 || dist[u][i] > dist[v][i] + w)) {
                //sp u,i ← v
                sp[u][i] = v;
                //update dist u,i with this new dist
                dist[u][i] = dist[v][i] + w;
                attach(c1, u, i);
                if (is_complete(u)) {
                    emit(c1, u);
                }
            }
            //if v spreads more activation to u from t i then
            if (at[u][i]/ (1-mu) < at[v][i]/(1-mu) * mu * 1 / g1.nodeInWeight.get(v)) {
                //update a u,i with this new activation
                atv[u] = atv[u] - at[u][i];
                at[u][i] = at[v][i]/(1-mu) * mu * 1 / g1.nodeInWeight.get(v) * (1-mu);
                atv[u] = atv[u] + at[u][i];

                activate(c1, u, i);
            }
        }
    }

    void attach(ConnectedGraph c1, int v, int k) {
        //update priority of v if it is present in Qin
        Qin.tryHigher(v, atv[k]);

        //dijkstra to propagate distance
        ArrayList<Integer> list = new ArrayList<>();
        PriorityQueue<HopVD> nodes = new PriorityQueue<>();
        nodes.add(new HopVD(v, dist[v][k]));
        while (!nodes.isEmpty()) {
            HopVD sh = nodes.poll();
            if (visited[sh.v]) {
                continue;
            }
            list.add(sh.v);
            visited[sh.v] = true;
            for (HopVDE edge : c1.getEdges(sh.v)) {
                if (depth[edge.v] < 0)	//withdraw none reached ancestor
                    continue;
                if (dist[sh.v][k] >= 0 &&
                        (dist[edge.v][k] < 0 || c1.getNodeWeight(edge.v) + dist[sh.v][k] < dist[edge.v][k])) {
                    dist[edge.v][k] = c1.getNodeWeight(edge.v) + dist[sh.v][k];
                    sp[edge.v][k] = sh.v;
                    nodes.add(new HopVD(edge.v, dist[edge.v][k]));
                }
            }
        }
        for (int i : list) {
            visited[i] = false;
        }
        list.clear();
    }

    void activate(ConnectedGraph c1, int v, int k) {
        //update priority of v if it is present in Qin
        Qin.tryHigher(v, atv[k]);

        ArrayList<Integer> list = new ArrayList<>();
        PriorityQueue<HopVD> nodes = new PriorityQueue<>();
        nodes.add(new HopVD(v, -at[v][k]/(1-mu)*mu));
        while (!nodes.isEmpty()) {
            //node with biggest atv to spread
            HopVD sh = nodes.poll();
            if (visited[sh.v]) {
                continue;
            }
            list.add(sh.v);
            atv[sh.v] = atv[sh.v] - at[sh.v][k];
            visited[sh.v] = true;

            for (HopVDE edge : c1.getEdges(sh.v)) {
                if (depth[edge.v] < 0 || dist[sh.v][k] <= dist[edge.v][k]) {
                    continue;
                }
                double newat = -sh.getDis()* 1 / g1.nodeInWeight.get(sh.v);
                if (newat*(1-mu) > at[edge.v][k]) {
                    at[edge.v][k] = newat*(1-mu);
                    nodes.add(new HopVD(edge.v, newat*mu));
                }
            }
        }
        for (int i : list) {
            visited[i] = false;
            atv[i] = atv[i] + at[i][k];
            Qin.tryHigher(i, atv[i]);
        };
        list.clear();
    }

    void emit(ConnectedGraph c1, int v) {
        //System.out.println("emit: "+ v);
        AnsTreeBanksIINode anst = new AnsTreeBanksIINode(c1, v);
        ArrayList<Integer> path = new ArrayList<>();
        //visited[v] = true;
        //generate tree
        for (int i = 0; i < keyNum; i++) {
            path.add(v);
            int tv = v;
            while (sp[tv][i] != tv) {
                tv = sp[tv][i];
                path.add(tv);
            }
            anst.addPath(path, dist, i);
            path.clear();
        }

        //store candidate tree in queue
        //topk tree
        if (ansQueue.size() < topk) {
            ansQueue.add(anst);
        }
        else {
            //a smaller tree
            if (ansQueue.peek().getWeight() > anst.getWeight()) {
                ansQueue.poll();
                ansQueue.add(anst);
            }
        }
    }

    boolean is_complete(int v) {
        for (double it : dist[v]) {
            if (it < 0) {
                return false;
            }
        }
        return true;
    }


    private void search() throws Exception {
        bestAns = null;
        setRepeatFlag();
        for (ConnectedGraph c1 : g1.cg) {
            if (!c1.keyContains(g1.keywordList)) {
                continue;
            }
            bidir_exp_search(c1);
        }
        ansList.clear();
        while (!ansQueue.isEmpty()) {
            ansList.add(ansQueue.poll());
        }

        for (AnsTreeBanksIINode abn : ansList) {
            if (bestAns == null || abn.getWeight() > bestAns.getWeight()) {
                bestAns = abn;
            }
        }
        //exception if anslist is empty
        if (bestAns == null) {
            throw new Exception("empty ans list");
        }
        bestAns.calcScore(Util.ALPHA);
        //bestAns.printTree();
    }

    @Override
    public void search(GraphBase gg, List<String> keywordList0, Map<String, List<Integer>> mp) throws Exception {
        startTime = System.currentTimeMillis();
        g1 = (GraphBanksII)gg;
        g1.givenQueryWord(keywordList0, mp);
        search();
    }

    @Override
    public void search(GraphBase gg, List<String> keywordList0) throws Exception {
        startTime = System.currentTimeMillis();
        g1 = (GraphBanksII)gg;
        g1.givenQueryWord(keywordList0);
        search();
    }
}
