package cn.net.bhe.flinkdemo.sourcedemo;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class KafkaSourceDemo {

    public static void main(String[] args) throws Exception {
        new KafkaSourceDemo().wordCount();
    }

    public void wordCount() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "192.168.233.129:9092,192.168.233.130:9092,192.168.233.131:9092");
        properties.setProperty("group.id", "consumer_word");
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
        properties.setProperty("auto.offset.reset", "latest");

        /* 数据入口 */
        DataStream<String> dataStream = environment.addSource(new FlinkKafkaConsumer<>("word", new SimpleStringSchema(), properties));

        /* 数据处理 */
        DataStream<Tuple2<String, Integer>> result = dataStream.flatMap(new WordCountFunction())
                .keyBy(tuple2 -> tuple2.f0)
                .sum(1);

        /* 数据出口 */
        result.print();

        /* 执行 */
        environment.execute();
    }

}
