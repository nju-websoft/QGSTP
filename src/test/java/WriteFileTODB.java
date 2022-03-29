import com.bosch.graphdeal.Util;
import com.bosch.graphdeal.WriteLUBM;
import com.bosch.graphdeal.WriteSubDBpedia;
import com.bosch.queryGen.QueryLUBM;
import com.bosch.queryGen.QuerySubDBpedia;

import java.io.*;

/**
 * @Author Yuxuan Shi
 * @Date 11/8/2019
 * @Time 12:33 PM
 * the class writes files to databases and generates queries
 */
public class WriteFileTODB {

    /**
     * write files to databases and generate queries
     *
     * @throws IOException
     */
    static void writeDBPEDIAFiles() throws IOException {
        WriteSubDBpedia d1 = new WriteSubDBpedia();
        d1.writeAll(Util.DBPEDIA, Util.DBPEDIA50K_SIZE);
        QuerySubDBpedia qd = new QuerySubDBpedia(Util.DBPEDIA);
        qd.filterQuery();
    }

    static void writeLUBMFiles() throws IOException {
        String file = Util.LUBM50U;
        WriteLUBM d1 = new WriteLUBM();
        d1.writeAll(file);
        QueryLUBM qd = new QueryLUBM(file);
        qd.generate();
    }

    public static void main(String[] args) {
        try {
            writeDBPEDIAFiles();
            //writeLUBMFiles();

            //Util.writeVector(Util.LUBM10U);
        } catch (OutOfMemoryError e) {
            Runtime.getRuntime().gc();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //SendMail.getInstance().send();
        }
    }
}
