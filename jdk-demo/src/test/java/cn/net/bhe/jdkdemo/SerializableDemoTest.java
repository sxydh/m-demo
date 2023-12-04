package cn.net.bhe.jdkdemo;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

@Log4j2
class SerializableDemoTest {

    @Test
    void write() throws Exception {
        new SerializableDemo().write();
    }

    @Test
    void read() throws Exception {
        log.info(new SerializableDemo().read());
    }

}