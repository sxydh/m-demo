package cn.net.bhe.kafkaclientdemo.producerdemo;

import cn.net.bhe.mutil.BoolUtils;
import cn.net.bhe.mutil.NmUtils;
import cn.net.bhe.mutil.NumUtils;
import cn.net.bhe.mutil.StrUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

@Slf4j
public class _Main {

    @Data
    @Accessors(chain = true)
    private static class UserProp {
        private String brokers = "192.168.233.129:9092,192.168.233.130:9092,192.168.233.131:9092";
        private String topic = "word";
        private Integer partition = null; // NumUtils.TWO
        private String partitioner = null; // PartitionerDemo.class.getName()
        private String compression = "snappy";
        private Boolean isAsync = true;
        private Boolean isTransaction = false;
    }

    public static void main(String[] args) {
        /* 创建生产者 */
        UserProp userProp = new UserProp();
        KafkaProducer<String, String> producer = buildProducer(userProp);

        /* 发送数据 */
        // 是否开启事务
        if (userProp.getIsTransaction()) {
            producer.initTransactions();
            producer.beginTransaction();
        }
        try {
            for (int i = 0; i < 10; i++) {
                ProducerRecord<String, String> record;
                String value = NmUtils.randomName();
                String key = String.valueOf(value.charAt(0));
                // 指定分区
                if (userProp.getPartition() != null) {
                    record = new ProducerRecord<>(userProp.getTopic(), userProp.getPartition(), key, value);
                }
                // 不指定分区
                else {
                    record = new ProducerRecord<>(userProp.getTopic(), key, value);
                }
                // 异步发送
                if (userProp.getIsAsync()) {
                    // 无回调
                    // producer.send(record);
                    // 有回调
                    producer.send(record, (metadata, exception) -> {
                        log.info(metadata.topic());
                        log.info(String.valueOf(metadata.partition()));
                    });
                }
                // 同步发送
                else {
                    producer.send(record, (metadata, exception) -> {
                        log.info(metadata.topic());
                        log.info(String.valueOf(metadata.partition()));
                    }).get();
                }
            }
            if (userProp.getIsTransaction()) {
                // 模拟异常
                if (BoolUtils.random()) {
                    throw new RuntimeException();
                }
                producer.commitTransaction();
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            if (userProp.getIsTransaction()) {
                producer.abortTransaction();
            }
        }

        /* 关闭资源 */
        producer.close();
    }

    private static KafkaProducer<String, String> buildProducer(UserProp userProp) {
        /* 生产者配置 */
        Properties properties = new Properties();
        // Broker列表
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, userProp.getBrokers());
        // Key序列化
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // Value序列化
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // 自定义分区器
        if (StrUtils.isNotEmpty(userProp.getPartitioner())) {
            properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, userProp.getPartitioner());
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
        if (StrUtils.isNotEmpty(userProp.getCompression())) {
            properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, userProp.getCompression());
        }
        // 应答类型
        // 默认1，Leader收到数据后应答。
        properties.put(ProducerConfig.ACKS_CONFIG, String.valueOf(NumUtils.MINUS_ONE));
        // 发送失败重试次数
        // 默认0
        properties.put(ProducerConfig.RETRIES_CONFIG, NumUtils.THREE);
        // 批量发送事务ID
        if (userProp.getIsTransaction()) {
            properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "transaction_id_01");
        }

        /* 创建生产者 */
        return new KafkaProducer<>(properties);
    }

}
