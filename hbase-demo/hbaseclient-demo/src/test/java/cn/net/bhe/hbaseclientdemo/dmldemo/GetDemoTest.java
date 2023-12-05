package cn.net.bhe.hbaseclientdemo.dmldemo;

import org.apache.hadoop.hbase.client.Result;
import org.junit.jupiter.api.Test;

class GetDemoTest {

    @Test
    void get() throws Exception {
        Result row = new GetDemo().get("sxydh", "app_log", "row12260474278686804", "colg1", "col1");
        Helper.print(row);
    }
}