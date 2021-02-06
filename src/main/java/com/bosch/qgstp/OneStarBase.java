package com.bosch.qgstp;

import com.bosch.graphdeal.ConnectedGraph;
import com.bosch.graphdeal.GraphBase;
import com.bosch.graphdeal.Util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class OneStarBase extends OneStar{

    private static OneStarBase instance = null;

    public static OneStarBase getInstance(){
        if (instance == null) {
            Properties pps = Util.getInitPPS();
            if (!"false".equals(pps.get("INIT"))) {
                instance = new OneStarBase();
            }
        }
        return instance;
    }
    public static void closeInstance() {
        instance = null;
    }

    @Override
    public void search(GraphBase og1, List<String> keywordList) throws Exception{
        ansTree = null;
        startTime = System.currentTimeMillis();
        setRepeatFlag();
        opt = Double.MAX_VALUE;
        gos = (GraphOneStar)og1;
        gos.givenQueryWord(keywordList);
        for (ConnectedGraph c1 : gos.cg) {
            if (!c1.keyContains(keywordList)) {
                continue;
            }
            goodSearch(c1, keywordList);
            assert c1.checkTreeCover(ansTree, keywordList);
        }
        if (ansTree == null) {
            throw new Exception("find no answer");
        }
        //ansTree.printTree();
    }

    @Override
    public void search(GraphBase og1, List<String> keywordList, Map<String, List<Integer>> mp) throws Exception{
        ansTree = null;
        startTime = System.currentTimeMillis();
        setRepeatFlag();
        opt = Double.MAX_VALUE;
        gos = (GraphOneStar)og1;
        gos.givenQueryWord(keywordList, mp);
        for (ConnectedGraph c1 : gos.cg) {
            if (!c1.keyContains(keywordList)) {
                continue;
            }
            goodSearch(c1, keywordList);
            assert c1.checkTreeCover(ansTree, keywordList);
        }
        if (ansTree == null) {
            throw new Exception("find no answer");
        }
    }
}
