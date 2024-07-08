package cn.net.bhe.jnademo;

public class _JnaApp {

    public static void main(String[] args) throws Exception {
        int port = JnaFs.INSTANCE.Fs();
        System.out.println(port);
        Thread.currentThread().join();
    }

}
