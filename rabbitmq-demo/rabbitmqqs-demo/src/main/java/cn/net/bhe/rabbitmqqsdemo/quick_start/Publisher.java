package cn.net.bhe.rabbitmqqsdemo.quick_start;

import cn.net.bhe.mutil.CpUtils;
import cn.net.bhe.rabbitmqqsdemo.helper.Conn;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.nio.charset.StandardCharsets;

public class Publisher {

    public static void main(String[] args) throws Exception {
        try (Connection connection = Conn.get()) {
            Channel channel = connection.createChannel();
            String queueName = "queue_company";
            channel.queueDeclare(queueName, false, false, false, null);
            for (int i = 0; i < 10000; i++) {
                String msg = CpUtils.ranChnCp();
                channel.basicPublish("", queueName, null, msg.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

}
