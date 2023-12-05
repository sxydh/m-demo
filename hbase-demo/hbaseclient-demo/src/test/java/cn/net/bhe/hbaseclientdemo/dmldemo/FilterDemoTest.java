package cn.net.bhe.hbaseclientdemo.dmldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.junit.jupiter.api.Test;

class FilterDemoTest {

    @Test
    void filter() throws Exception {
        ResultScanner scanner = new FilterDemo().filter("sxydh", "app_log", "", "", "colg1", "col1", "觃絮宼瘦艋瀌同轶夬洤誟胥秐茘飼鷐酂煂嵬熐");
        for (Result row : scanner) {
            Helper.print(row);
        }
        CreateConnDemo.closeConn(scanner);
    }
}