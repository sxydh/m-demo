package cn.net.bhe.rabbitmqqsdemo.quick_start;

import cn.net.bhe.rabbitmqqsdemo.helper.Conn;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.nio.charset.StandardCharsets;

public class Receiver {

    public static void main(String[] args) throws Exception {
        try (Connection connection = Conn.get()) {
            Channel channel = connection.createChannel();
            String queueName = "queue_company";
            channel.basicConsume(
                    queueName,
                    true,
                    (consumerTag, delivery) -> {
                        String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
                        System.out.printf("[%s] Received - %s \r\n", queueName, msg);
                    },
                    cancelCallback -> {
                    });
            Thread.currentThread().join();
        }
    }

}
