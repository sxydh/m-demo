package cn.net.bhe.kafkaclientdemo.consumerdemo;

import cn.net.bhe.mutil.ArrUtils;
import cn.net.bhe.mutil.LiUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;

@Slf4j
public class _Main {

    @Data
    @Accessors(chain = true)
    private static class UserProp {
        private String brokers = "192.168.233.129:9092,192.168.233.130:9092,192.168.233.131:9092";
        private String topic = "word";
        private String groupId = "group_id_word_01";
        private Integer[] partitions = new Integer[]{0, 1, 2}; // new Integer[]{0, 1, 2}
        private Long[] offsets = null; // new Long[]{System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis()}
        private Boolean isOffsetTs = true;
        private Boolean autoCommit = false;
    }

    public static void main(String[] args) {
        /* 创建消费者 */
        UserProp userProp = new UserProp();
        KafkaConsumer<String, String> consumer = buildConsumer(userProp);

        /* 消费数据 */
        boolean flag = true;
        while (flag) {
            // 获取队列数据
            // 如果有数据poll会立即返回，否则按给定的时间原地等待。注意等待时间可能会超过给定值。
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                log.info(String.valueOf(record));
            }
            // 手动提交offset
            // 默认自动提交，自动提交间隔时间通过auto.commit.interval.ms设置，默认5s。
            if (!userProp.getAutoCommit()) {
                consumer.commitSync();
            }
        }

        /* 关闭资源 */
        consumer.close();
    }

    private static KafkaConsumer<String, String> buildConsumer(UserProp userProp) {
        /* 消费者配置 */
        Properties properties = new Properties();
        // Broker集群
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, userProp.getBrokers());
        // Key序列化
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Value序列化
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // 消费者组标识
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, userProp.getGroupId());
        // 消费者组的分区分配策略
        properties.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
        // 是否自动提交offset
        // 默认自动提交
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, userProp.getAutoCommit());
        // 自动提交offset间隔时间
        // 默认5s
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000);

        /* 创建消费者 */
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        initConsumer(userProp, consumer);
        return consumer;
    }

    private static void initConsumer(UserProp userProp, Consumer<String, String> consumer) {
        // 按主题消费
        if (ArrUtils.isEmpty(userProp.getPartitions())) {
            consumer.subscribe(LiUtils.of(userProp.getTopic()));
        }
        // 按分区消费
        else {
            List<TopicPartition> topicPartitions = new ArrayList<>();
            for (Integer partition : userProp.getPartitions()) {
                topicPartitions.add(new TopicPartition(userProp.getTopic(), partition));
            }
            consumer.assign(topicPartitions);
        }
        // 手动设置消费开始位置
        // 默认从上次位置继续消费
        if (ArrUtils.isNotEmpty(userProp.getOffsets())) {
            Iterator<TopicPartition> partitionIterator = consumer.assignment().iterator();
            // 根据时间戳指定offset
            if (userProp.getIsOffsetTs()) {
                Map<TopicPartition, Long> tplMap = new HashMap<>();
                for (Long offset : userProp.getOffsets()) {
                    tplMap.put(partitionIterator.next(), offset);
                }
                Map<TopicPartition, OffsetAndTimestamp> tpotMap = consumer.offsetsForTimes(tplMap);
                for (Map.Entry<TopicPartition, OffsetAndTimestamp> tpotEntry : tpotMap.entrySet()) {
                    if (tpotEntry.getValue() != null) {
                        consumer.seek(tpotEntry.getKey(), tpotEntry.getValue().offset());
                    }
                }
            }
            // 显示指定offset
            else {
                for (Long offset : userProp.getOffsets()) {
                    consumer.seek(partitionIterator.next(), offset);
                }
            }
        }
    }

}
