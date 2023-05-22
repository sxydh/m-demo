package cn.net.bhe.rbmsconsumer.service;

import cn.net.bhe.rbmscommon.RabbitMqConstants;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    @RabbitListener(queues = RabbitMqConstants.TOPIC_QUEUE1)
    public void consumeTopicQueue1(String msg, Message message) {
        System.out.println(msg);
        System.out.println(message);
    }

    @RabbitListener(queues = RabbitMqConstants.TOPIC_QUEUE2)
    public void consumeTopicQueue2(String msg, Message message) {
        System.out.println(msg);
        System.out.println(message);
    }

    @RabbitListener(queues = RabbitMqConstants.TOPIC_QUEUE3)
    public void consumeTopicQueue3(String msg, Message message) {
        System.out.println(msg);
        System.out.println(message);
    }

}
