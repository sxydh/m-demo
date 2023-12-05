package cn.net.bhe.hbaseclientdemo.ddldemo;

import org.junit.jupiter.api.Test;

class CreateTableDemoTest {

    @Test
    void createTable() throws Exception {
        new CreateTableDemo().createTable("sxydh", "app_log", "colg1", "colg2");
    }

    @Test
    void modifyTable() throws Exception {
        new CreateTableDemo().modifyTable("sxydh", "app_log", "colg1");
    }
}