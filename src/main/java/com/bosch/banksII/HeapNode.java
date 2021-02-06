package com.bosch.banksII;

/**
 * @Author Yuxuan Shi
 * @Date 10/31/2019
 * @Time 9:54 AM
 */
public class HeapNode {
    int v;
    double activation;
    HeapNode(int v, double activation){
        this.v = v;
        this.activation = activation;
    }
    HeapNode(int v, double activation, int depth){
        this.v = v;
        this.activation = activation;
    }
}
