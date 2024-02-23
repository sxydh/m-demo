package cn.net.bhe.mqttclientdemo;

import cn.net.bhe.mutil.StrUtils;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

@Log4j2
public class ClientDemo {

    private final MqttClient mqttClient;

    @SuppressWarnings("BusyWait")
    public ClientDemo(String pubTopic, String subTopic, int qos, String broker, String clientId) throws Exception {
        /* 建立连接 */
        mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName("admin");
        connOpts.setPassword("public".toCharArray());
        // 会话设置
        connOpts.setCleanSession(true);
        // 回调设置
        mqttClient.setCallback(new AnyCallback());
        mqttClient.connect(connOpts);

        /* 发布消息 */
        new Thread(() -> {
            try {
                while (true) {
                    MqttMessage message = new MqttMessage(StrUtils.randomPhone().getBytes());
                    message.setQos(qos);
                    mqttClient.publish(pubTopic, message);
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }).start();

        /* 订阅主题 */
        mqttClient.subscribe(subTopic);
    }

    static class AnyCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            log.warn("连接断开 - {}", cause.getLocalizedMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            log.info("收到消息 [{}] - {}", topic, message.getPayload());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            log.info("发布消息结果 - {}", token.isComplete());
        }

    }

}
