package cn.net.bhe.jnademo;

public class _JnaApp {

    public static void main(String[] args) {
        int port = JnaFs.INSTANCE.Fs();
        System.out.println(port);
    }

}
