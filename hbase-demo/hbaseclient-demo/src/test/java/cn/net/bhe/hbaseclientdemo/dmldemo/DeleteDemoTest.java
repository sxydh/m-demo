package cn.net.bhe.hbaseclientdemo.dmldemo;

import org.junit.jupiter.api.Test;

class DeleteDemoTest {

    @Test
    void delete() throws Exception {
        new DeleteDemo().delete("sxydh", "app_log", "row1000895", "colg1", "col1");
    }
}