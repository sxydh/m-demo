package cn.net.bhe.rabbitmqqsdemo.helper;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Conn {

    private static final ConnectionFactory FACTORY = new ConnectionFactory();

    static {
        FACTORY.setHost("192.168.233.129");
        FACTORY.setUsername("admin");
        FACTORY.setPassword("123");
    }

    public static Connection get() throws Exception {
        return FACTORY.newConnection();
    }

}
