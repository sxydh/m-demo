package cn.net.bhe.hbaseclientdemo.dmldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.junit.jupiter.api.Test;

class ScanDemoTest {

    @Test
    void scan() throws Exception {
        ResultScanner scanner = new ScanDemo().scan("sxydh", "app_log", "row12260474278686804", "row12260474278686806");
        for (Result row : scanner) {
            Helper.print(row);
        }
        CreateConnDemo.closeConn(scanner);
    }
}