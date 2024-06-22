package cn.net.bhe.jdkdemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class UnsafeDemoTest {

    @Test
    void allocateMemory() {
        new UnsafeDemo().allocateMemory();
    }

    @Test
    void allocateInstance() throws Exception {
        new UnsafeDemo().allocateInstance();
    }

    @Test
    void putInt() throws Exception {
        new UnsafeDemo().putInt();
    }

    @Test
    void compareAndSwapLong() throws Exception {
        new UnsafeDemo().compareAndSwapLong();
    }

    @Test
    void park() {
        new UnsafeDemo().park();
    }

    @Test
    void unpark() throws Exception {
        new UnsafeDemo().unpark();
    }
}