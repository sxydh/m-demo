package cn.net.bhe.flinkdemo.sinkdemo;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;

public class KafkaSinkDemo {

    public static void main(String[] args) throws Exception {
        new KafkaSinkDemo().wordSave();
    }

    public void wordSave() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据出口 */
        // 数据保存到Kafka
        dataStream.addSink(new FlinkKafkaProducer<>("192.168.233.129:9092,192.168.233.130:9092,192.168.233.131:9092", "word", new SimpleStringSchema()));

        /* 执行 */
        environment.execute();
    }

}
