package com.bosch.graphdeal;

import java.util.List;
import java.util.Map;

/**
 * @author Yuxuan Shi
 * @date 2020/5/25
 */
public abstract class SearchBase {
    abstract public AnsTree getAnsTree();

    abstract public JsonTree JsonGenerate();

    /**
     * start time of the algorithm
     */
    protected long startTime;

    /**
     * @return true if the process should be repeated
     */
    public boolean isRepeatFlag() {
        return repeatFlag;
    }

    public void setRepeatFlag() {
        startTime = System.currentTimeMillis();
        this.repeatFlag = true;
    }

    /**
     * to mark whether search should be repeated
     */
    private boolean repeatFlag = true;

    /**
     * check time out
     *
     * @return true if time out
     */
    protected boolean isTimeOut() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime > Util.REPIME) {
            repeatFlag = false;
        }
        if (!Util.CHECK_TIMEOUT) {
            return false;
        }
        if (currentTime - startTime > Util.CUTIME) {
            return true;
        }
        return false;
    }

    /**
     * the entry point of search algorithm
     *
     * @param gg           graph
     * @param keywordList0 keyword
     * @param mp           map of keyword
     * @throws Exception
     */
    public void search(GraphBase gg, List<String> keywordList0, Map<String, List<Integer>> mp) throws Exception {
        throw new Exception("need to be implemented");
    }

    /**
     * search query
     *
     * @param gg           graph
     * @param keywordList0 keyword
     * @throws Exception
     */
    abstract public void search(GraphBase gg, List<String> keywordList0) throws Exception;
}
