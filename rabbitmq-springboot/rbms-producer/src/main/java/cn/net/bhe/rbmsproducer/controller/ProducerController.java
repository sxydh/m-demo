package cn.net.bhe.rbmsproducer.controller;

import cn.net.bhe.rbmscommon.RabbitMqConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/producer")
public class ProducerController {

    private RabbitTemplate rabbitTemplate;

    public ProducerController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            System.out.println(correlationData);
            System.out.println(ack);
            System.out.println(cause);
            System.out.println();
        });
        this.rabbitTemplate.setReturnsCallback(returnCallback -> {
            System.out.println(returnCallback);
            System.out.println();
        });
    }

    @PostMapping("sendByTopic")
    public void sendByTopic(@RequestParam("routingKey") String routingKey, @RequestParam("msg") String msg) {
        rabbitTemplate.convertAndSend(RabbitMqConstants.TOPIC_EXCHANGE, routingKey, msg);
    }
}
