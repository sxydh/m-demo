package cn.net.bhe.kafkapetdemo.producer;

import cn.net.bhe.kafkapetdemo.helper.DataHelper;
import cn.net.bhe.kafkapetdemo.helper.ProducerHelper;
import cn.net.bhe.kafkapetdemo.helper.ProducerHelper.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class _Main {

    public static void main(String[] args) {
        int n = 10000000;
        int threads = 1;
        int allocSec = 3600;

        List<String> files = DataHelper.create(n, threads, allocSec);
        ExecutorService executorService = Executors.newFixedThreadPool(files.size());
        for (String file : files) {
            executorService.submit(() -> {
                try {
                    long mills = System.currentTimeMillis();

                    Config config = new Config();
                    Map<String, Object> res = send(config, file);

                    mills = System.currentTimeMillis() - mills;
                    long size = (int) res.get("size");
                    int tps = (int) (size / (mills / 1000.0));
                    TreeMap<Long, BigDecimal> rtMap = new TreeMap<>();
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) res.get("rtMap")).entrySet()) {
                        int value = (int) entry.getValue();
                        BigDecimal calculatedValue = BigDecimal.valueOf(value * 1.0 / size * 100).setScale(2, RoundingMode.HALF_UP);
                        rtMap.put((Long) entry.getKey(), calculatedValue);
                    }
                    System.out.println("[" + Thread.currentThread().getName().trim() + "] " + "size = " + size + ", tps = " + tps + ", rtMap = " + rtMap);
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            });
        }
    }

    private static Map<String, Object> send(Config config, String file) throws Exception {
        try (KafkaProducer<String, String> producer = ProducerHelper.buildProducer(config)) {
            int size = 0;
            Map<Long, Integer> rtMap = new HashMap<>();
            try (FileReader fileReader = new FileReader(file)) {
                try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                    while (true) {
                        long start = System.currentTimeMillis();
                        String line = bufferedReader.readLine();
                        if (line == null) break;

                        doSend(config, producer, String.valueOf(size), line);

                        size++;
                        Long rt = (System.currentTimeMillis() - start) / 10 * 10;
                        Integer rtCount = rtMap.get(rt);
                        if (rtCount == null) {
                            rtCount = 1;
                        } else {
                            rtCount++;
                        }
                        rtMap.put(rt, rtCount);
                    }
                }
            }
            Map<String, Object> res = new HashMap<>();
            res.put("size", size);
            res.put("rtMap", rtMap);
            return res;
        }
    }

    private static void doSend(Config config, KafkaProducer<String, String> producer, String key, String value) throws Exception {
        ProducerRecord<String, String> record;
        // 指定分区
        if (config.getPartition() != null) {
            record = new ProducerRecord<>(config.getTopic(), config.getPartition(), key, value);
        }
        // 不指定分区
        else {
            record = new ProducerRecord<>(config.getTopic(), key, value);
        }
        // 异步发送
        if (config.getIsAsync()) {
            producer.send(record, (metadata, exception) -> {
                // TODO SOMETHING
            });
        }
        // 同步发送
        else {
            producer.send(record, (metadata, exception) -> {
                // TODO SOMETHING
            }).get();
        }
    }

}
