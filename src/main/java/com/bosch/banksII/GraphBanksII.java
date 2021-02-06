package com.bosch.banksII;

import com.bosch.graphdeal.GraphBase;
import com.bosch.graphdeal.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Author Yuxuan Shi
 * @Date 10/31/2019
 * @Time 9:44 AM
 */
public class GraphBanksII extends GraphBase {
    public List<Double> nodeInWeight;

    private static GraphBanksII instance = null;
    public static GraphBanksII getInstance(){
        if (instance == null) {
            if (!Util.BOSCHKG) {
                Properties pps = Util.getInitPPS();
                if (!"false".equals(pps.get("INIT"))) {
                    instance = new GraphBanksII(pps.get("DATABASE").toString(),
                            pps.get("GRAPH_NAME").toString());
                }
            } else {
                instance = new GraphBanksII("C:\\kgdata\\inallrdf\\triples",
                        "C:\\kgdata\\inallrdf\\atype","boschkg");
            }
        }
        return instance;
    }

    public static void closeInstance() {
        instance = null;
    }

    @Override
    protected void graphInit(){
        nodeInWeight = new ArrayList<>(biggestNodeNum);
    }

    /*public GraphBanksII(DogReader dbr, String graphName){
        readGraph(dbr, graphName);
    }*/

    public GraphBanksII(String database, String graphName){
        readGraph(database, graphName);
    }

    public GraphBanksII(String fileOrDir, String typeFileOrDir, String graphName){
        readGraph(fileOrDir, typeFileOrDir, graphName);
    }
}
