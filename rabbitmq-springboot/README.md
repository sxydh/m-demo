# 快速上手

SpringBoot集成RabbitMQ简单示例

## RabbitMQ安装

以docker方式安装RabbitMQ：  

```bash
# pull镜像
docker pull rabbitmq:management

# 启动RabbitMQ
docker run -d --hostname rabbitmq_host --name rabbitmq -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=passwd -p 15672:15672 -p 5672:5672 rabbitmq:management
```

安装完成后创建name为`/dev`的Virtual Hosts，给项目测试使用。  

## SpringBoot集成

生产者服务配置`application.yaml`：  

```yaml
spring:
  rabbitmq:
    host: 192.168.30.130
    port: 5672
    virtual-host: /dev
    username: admin
    password: passwd
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      mandatory: true
```

消费者服务配置`application.yaml`：  

```yaml
spring:
  rabbitmq:
    host: 192.168.30.130
    port: 5672
    virtual-host: /dev
    username: admin
    password: passwd
```

启动生产者服务，调用接口发送消息：  

```bash
curl --location --request POST 'http://127.0.0.1:50010/producer/sendByTopic?msg=msg1&routingKey=topicQueue1.1'
```

启动消费者服务，消费队列消息：  

```java
@RabbitListener(queues = RabbitMqConstants.TOPIC_QUEUE1)
public void consumeTopicQueue1(String msg, Message message) {
    System.out.println(msg);
    System.out.println(message);
}
```
