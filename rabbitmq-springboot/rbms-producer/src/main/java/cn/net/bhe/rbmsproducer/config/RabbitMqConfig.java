package cn.net.bhe.rbmsproducer.config;

import cn.net.bhe.rbmscommon.RabbitMqConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Exchange topicExchange() {
        return ExchangeBuilder.topicExchange(RabbitMqConstants.TOPIC_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue topicQueue1() {
        return QueueBuilder.durable(RabbitMqConstants.TOPIC_QUEUE1).build();
    }

    @Bean
    public Queue topicQueue2() {
        return QueueBuilder.durable(RabbitMqConstants.TOPIC_QUEUE2).build();
    }

    @Bean
    public Queue topicQueue3() {
        return QueueBuilder.durable(RabbitMqConstants.TOPIC_QUEUE3).build();
    }

    @Bean
    public Binding bindingTopicQueue1(Queue topicQueue1, Exchange exchange) {
        return BindingBuilder.bind(topicQueue1).to(exchange).with("topicQueue1.*").noargs();
    }

    @Bean
    public Binding bindingTopicQueue2(Queue topicQueue2, Exchange exchange) {
        return BindingBuilder.bind(topicQueue2).to(exchange).with("topicQueue2.*").noargs();
    }

    @Bean
    public Binding bindingTopicQueue3(Queue topicQueue3, Exchange exchange) {
        return BindingBuilder.bind(topicQueue3).to(exchange).with("topicQueue3.*").noargs();
    }

}
