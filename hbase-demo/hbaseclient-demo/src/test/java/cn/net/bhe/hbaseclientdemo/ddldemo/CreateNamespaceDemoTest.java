package cn.net.bhe.hbaseclientdemo.ddldemo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class CreateNamespaceDemoTest {

    @Test
    void createNamespace() throws Exception {
        new CreateNamespaceDemo().createNamespace("sxydh");
    }
}