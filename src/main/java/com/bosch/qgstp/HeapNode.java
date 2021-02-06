package com.bosch.qgstp;


import com.bosch.graphdeal.CompareScore;
import org.jetbrains.annotations.NotNull;

/**
 * @Author Yuxuan Shi
 * @Date 2/4/2020
 * @Time 11:00 AM
 */
class HeapNode implements CompareScore<HeapNode>, Comparable<HeapNode>{
    private int u;
    private int k;
    private double score;

    HeapNode(int v, int k, double score) {
        this.u = v;
        this.k = k;
        this.score = score;
    }
    public int getU() {
        return u;
    }
    public int getK() {
        return k;
    }
    public double getScore() {
        return score;
    }
    @Override
    public int compareScore(HeapNode h2) {
        return Double.compare(score, h2.score);
    }

    @Override
    public int compareTo(@NotNull HeapNode h2) {
        int result = Integer.compare(u, h2.u);
        if (result == 0) result = Integer.compare(k, h2.k);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return compareTo((HeapNode) o) == 0;

    }

    @Override
    public int hashCode() {
        return u;
    }
}
