package cn.bet.bhe.javaagent.bytebuddy;

import org.junit.jupiter.api.Test;

class CreatingAClassTest {

    private final CreatingAClass creatingAClass = new CreatingAClass();

    @Test
    void subclass() {
        try {
            creatingAClass.subclass();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void load() {
        creatingAClass.load();
    }

}