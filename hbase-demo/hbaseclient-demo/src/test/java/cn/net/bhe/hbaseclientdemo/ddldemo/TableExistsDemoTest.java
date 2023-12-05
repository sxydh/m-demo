package cn.net.bhe.hbaseclientdemo.ddldemo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class TableExistsDemoTest {

    @Test
    void tableExists() throws Exception {
        boolean b = new TableExistsDemo().tableExists("sxydh", "app_log");
        log.info(String.valueOf(b));
    }
}