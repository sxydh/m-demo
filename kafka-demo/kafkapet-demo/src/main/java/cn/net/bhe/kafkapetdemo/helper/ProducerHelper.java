package cn.net.bhe.kafkapetdemo.helper;

import cn.net.bhe.mutil.NumUtils;
import cn.net.bhe.mutil.StrUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerHelper {

    @Data
    @Accessors(chain = true)
    public static class Config {
        private String brokers = "192.168.233.129:9092,192.168.233.130:9092,192.168.233.131:9092";
        private String topic = "pet_order";
        private Integer partition = null;
        private String partitioner = null;
        private String compression = null;
        private Boolean isAsync = true;
    }

    public static KafkaProducer<String, String> buildProducer(Config config) {
        Properties properties = new Properties();
        // Broker列表
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBrokers());
        // Key序列化
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // Value序列化
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // 自定义分区器
        if (StrUtils.isNotEmpty(config.getPartitioner())) {
            properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, config.getPartitioner());
        }
        // 缓冲区大小
        // 默认32m
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432 * 2);
        // 批次大小
        // 默认16k
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384 * 8);
        // 每批次数据缓冲时间阈值
        // 默认0ms
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        // 压缩类型
        // 支持gzip/snappy/lz4/zstd
        // 默认不压缩
        if (StrUtils.isNotEmpty(config.getCompression())) {
            properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, config.getCompression());
        }
        // 应答类型
        // 默认1，Leader收到数据后应答。
        properties.put(ProducerConfig.ACKS_CONFIG, String.valueOf(NumUtils.ONE));
        // 发送失败重试次数
        // 默认0
        properties.put(ProducerConfig.RETRIES_CONFIG, NumUtils.THREE);
        return new KafkaProducer<>(properties);
    }

}
