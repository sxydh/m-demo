package cn.net.bhe.rabbitmqqsdemo.exchange_direct;

import cn.net.bhe.mutil.CpUtils;
import cn.net.bhe.rabbitmqqsdemo.helper.Conn;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.nio.charset.StandardCharsets;
import java.util.Random;

@SuppressWarnings("DuplicatedCode")
public class Producer {

    private static final String EXCHANGE = "exchange_direct";
    private static final String ROUTING_KEY_ONE = "key_one";
    private static final String ROUTING_KEY_TWO = "key_two";

    public static void main(String[] args) throws Exception {
        try (Connection connection = Conn.get()) {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT.getType());
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
