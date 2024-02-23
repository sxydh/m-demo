package cn.net.bhe.mqttclientdemo;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class _Main {

    public static void main(String[] args) throws Exception {
        String topic11 = "topic_test/11";
        String topic12 = "topic_test/12";
        int qos = 2;
        String broker = "tcp://192.168.233.129:1883";

        new ClientDemo(topic11, topic12, qos, broker, "client_11");
        new ClientDemo(topic12, topic11, qos, broker, "client_12");
    }

}
