package com.bosch.qgstp;

/**
 * @Author yuxuan shi
 * @Date 2020/10/11
 * @Time 10:39
 **/
public class PruneControl {
    boolean pruneSmallN;
    boolean pruneLargeN;
    boolean pruneLargeM;
    boolean rootVertexRanking;

    public boolean isPruneLargeN() {
        return pruneLargeN;
    }

    public boolean isPruneLargeM() {
        return pruneLargeM;
    }

    public boolean isRootVertexRanking() {
        return rootVertexRanking;
    }

    public boolean isPruneSmallN() {
        return pruneSmallN;
    }

    /**
     * default setting is to use all pruning strategies
     */
    public PruneControl() {
        pruneSmallN = true;
        pruneLargeN = true;
        pruneLargeM = true;
        rootVertexRanking = true;
    }

    /**
     * set parameter
     *
     * @param mode number vary 0-15
     */
    public PruneControl(int mode) {
        if (mode < 0 || mode > 0b1111) {
            return;
        }
        pruneSmallN = (mode & 0b1000) != 0;
        pruneLargeN = (mode & 0b0100) != 0;
        pruneLargeM = (mode & 0b0010) != 0;
        rootVertexRanking = (mode & 0b0001) != 0;
    }
}
