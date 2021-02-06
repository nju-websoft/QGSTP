import com.bosch.graphdeal.AnsTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class QueryInfo {
    List<String> query;
    String alg;
    AnsTree ans;
    double midTime;
    List<Double> timeList;

    QueryInfo(List<String> query, String alg) {
        this.query = query;
        this.alg = alg;
        timeList = new ArrayList<>();
        ans = null;
    }

    double getFirstTime() {
        if (timeList.size() == 0) return 0D;
        return timeList.get(0);
    }

    void addTime(double time) {
        timeList.add(time);
    }

    double getTime() {
        return midTime;
    }

    void setAns(AnsTree ans) {
        this.ans = ans;
    }

    AnsTree getAns() {
        return ans;
    }

    void calcMid() {
        Collections.sort(timeList);
        if (timeList.size() == 0) {
            midTime = 0D;
            return;
        }
        if ((timeList.size() % 2) == 1) {
            midTime = timeList.get(timeList.size() / 2);
            return;
        }
        //now timelist has even number
        midTime = (timeList.get(timeList.size() / 2) + timeList.get(timeList.size() / 2 - 1)) / 2;
    }

    double getScore() {
        if (ans == null)
            return -1D;
        return ans.getScore();
    }

    double getSal() {
        if (ans == null)
            return -1D;
        return ans.getSal();
    }

    double getCoh() {
        if (ans == null)
            return -1D;
        return ans.getCoh();
    }

    String queryString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < query.size() - 1; i++) {
            sb.append(query.get(i));
            sb.append(";");
        }
        sb.append(query.get(query.size() - 1));
        return sb.toString();
    }
}
