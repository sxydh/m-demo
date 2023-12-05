package cn.net.bhe.hbaseclientdemo.dmldemo;

import org.junit.jupiter.api.Test;

class PutDemoTest {

    @Test
    void put() throws Exception {
        new PutDemo().put("sxydh", "app_log", "colg1");
    }
}