package cn.net.bhe.hbaseclientdemo.ddldemo;

import org.junit.jupiter.api.Test;

class DeleteTableDemoTest {

    @Test
    void deleteTable() throws Exception {
        new DeleteTableDemo().deleteTable("sxydh", "app_log");
    }
}