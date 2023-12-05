package cn.net.bhe.hbaseclientdemo.ddldemo;

import org.junit.jupiter.api.Test;

class DeleteNamespaceDemoTest {

    @Test
    void deleteNamespace() throws Exception {
        new DeleteNamespaceDemo().deleteNamespace("sxydh");
    }
}