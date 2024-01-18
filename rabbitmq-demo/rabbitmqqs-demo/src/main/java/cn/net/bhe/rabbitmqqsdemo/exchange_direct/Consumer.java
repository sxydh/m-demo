package cn.net.bhe.rabbitmqqsdemo.exchange_direct;

import cn.net.bhe.rabbitmqqsdemo.helper.Conn;
import cn.net.bhe.rabbitmqqsdemo.helper.ThreadPool;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@SuppressWarnings("DuplicatedCode")
public class Consumer {

    private static final String EXCHANGE = "exchange_direct";
    private static final String QUEUE_NAME_ONE = "queue_one";
    private static final String QUEUE_NAME_TWO = "queue_two";
    private static final String ROUTING_KEY_ONE = "key_one";
    private static final String ROUTING_KEY_TWO = "key_two";
    private static final AtomicLong COUNT = new AtomicLong(0);

    static {
        try (Connection connection = Conn.get()) {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT.getType());
            channel.queueDeclare(QUEUE_NAME_ONE, false, false, false, null);
            channel.queueDeclare(QUEUE_NAME_TWO, false, false, false, null);
            channel.queueBind(QUEUE_NAME_ONE, EXCHANGE, ROUTING_KEY_ONE);
            channel.queueBind(QUEUE_NAME_TWO, EXCHANGE, ROUTING_KEY_TWO);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String[] queueNames = {QUEUE_NAME_ONE, QUEUE_NAME_TWO};
        for (String queueName : queueNames) {
            // 模拟一个队列由多个客户端消费
            for (int i = 0; i < 1; i++) {
                ThreadPool.submit(() -> {
                    try {
                        consume(queueName);
                    } catch (Throwable e) {
                        log.error(e.getLocalizedMessage(), e);
                    }
                });
            }
        }
    }

    private static void consume(String queueName) throws Exception {
        try (Connection connection = Conn.get()) {
            Channel channel = connection.createChannel();
            channel.basicConsume(
                    queueName,
                    true,
                    (consumerTag, delivery) -> {
                        COUNT.addAndGet(1);
                        String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
                        System.out.printf("[%s] [%s] Received %s - %s \r\n", queueName, Thread.currentThread().getName(), COUNT, msg);
                    },
                    cancelCallback -> {
                    });
            Thread.currentThread().join();
        }
    }

}
