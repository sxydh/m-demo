package cn.net.bhe.rabbitmqqsdemo.exchange_default;

import cn.net.bhe.mutil.CpUtils;
import cn.net.bhe.rabbitmqqsdemo.helper.Conn;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.nio.charset.StandardCharsets;
import java.util.Random;

@SuppressWarnings("DuplicatedCode")
public class Producer {

    private static final String EXCHANGE = "";
    private static final String ROUTING_KEY_ONE = "queue_one";
    private static final String ROUTING_KEY_TWO = "queue_two";

    public static void main(String[] args) throws Exception {
        try (Connection connection = Conn.get()) {
            Channel channel = connection.createChannel();
            Random random = new Random();
            String[] keys = {ROUTING_KEY_ONE, ROUTING_KEY_TWO};
            for (int i = 0; i < 10000; i++) {
                String msg = CpUtils.ranChnCp();
                channel.basicPublish(EXCHANGE, keys[random.nextInt(keys.length)], null, msg.getBytes(StandardCharsets.UTF_8));
                Thread.sleep(500);
            }
        }
    }

}
