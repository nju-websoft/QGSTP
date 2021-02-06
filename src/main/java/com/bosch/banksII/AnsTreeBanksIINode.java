package com.bosch.banksII;

import com.bosch.graphdeal.AnsTree;
import com.bosch.graphdeal.ConnectedGraph;

import java.util.AbstractMap;
import java.util.List;

/**
 * @Author Yuxuan Shi
 * @Date 3/26/2020
 * @Time 11:07 AM
 */
public class AnsTreeBanksIINode extends AnsTree {
    private double weight;

    AnsTreeBanksIINode(ConnectedGraph c1, int r) {
        super(c1, r);
        weight = c1.getNodeWeight(r);
    }

    //add path with node weight
    void addPath(List<Integer> path, double[][] dist, int keyID) {
        if (path.size() == 0) {
            return;
        }
        if (nodes.contains(path.get(path.size() - 1))) {
            return;
        }

        //merge path to tree
        for (int j = path.size() - 1; j >= 0; j--) {
            int v = path.get(j);
            //add path length to tree
            if (nodes.contains(v)) {
                weight += dist[path.get(j + 1)][keyID];
                return;
            }
            nodes.add(v);
            edges.add(new AbstractMap.SimpleEntry<>(path.get(j - 1), path.get(j)));
        }
    }

    boolean checkpath(List<Integer> path, double[][] dist, int keyID) {
        double ans = 0D;
        for (int j = path.size() - 1; j >= 0; j--) {
            int v = path.get(j);
            ans = ans + c1.getNodeWeight(v);
            if (Math.abs(ans - dist[v][keyID]) > 10e-8) {
                return false;
            }
        }
        return true;
    }

    public double getWeight() {
        return weight;
    }

}
