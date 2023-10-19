package cn.net.bhe.testelasticsearch;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.experimental.Accessors;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.search.SearchHits;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试RefreshPolicy不同策略下的性能：NONE，IMMEDIATE。<br/>
 * <br/>
 * 整体方案：<br/>
 * <li>设置RefreshPolicy</li>
 * <li>将数据保存后立即查询该文档数据</li>
 * <li>分析更新延迟：查看是否存在查不到文档的情况。</li>
 * <li>分析并发性能：平均并发 = 测试数据量 / 测试时间。</li>
 * <br/>
 * 代码设计：<br/>
 * <li>队列：存放即将保存的文档数据。入队和出队都是多线程的，需要使用线程安全的队列实现。</li>
 * <li>生产者：生成即将保存的文档数据。为了保证队列有数据可用，生产者需要支持多线程生成文档数据。</li>
 * <li>消费者：从队列取出数据，保存后立即查询。为了模拟多用户场景，消费者支持多线程，保存和查询在同一个线程内完成（符合一般业务场景）。</li>
 * <li>结果保存：测试结束后使用csv文件存储结果数据，包含文档主键，保存时间戳，查询时间戳，是否命中等字段。</li>
 * <li>配置支持：通过JVM参数来配置应用参数，包含生产者数量，消费者数量，索引名称，文档主键前缀，文档值前缀，RefreshPolicy，测试数据量。</li>
 *
 * @author sxydh
 * @since 1.0
 */
public class TestRefreshPolicy {

    public static void main(String[] args) throws Exception {
        TestRefreshPolicy.execute();
    }

    public static void execute() throws Exception {
        for (int i = 0; i < Manager.PRODUCER_COUNT; i++) {
            Manager.EXECUTOR_SERVICE.execute(new ProducerJob(Manager.QUEUE, Manager.SIZE));
        }
        for (int i = 0; i < Manager.CONSUMER_COUNT; i++) {
            Manager.EXECUTOR_SERVICE.execute(new ConsumerJob(Manager.QUEUE, Manager.SIZE, Manager.RESULT_QUEUE));
        }
        Manager.PRODUCER_LATCH.await();
        Manager.CONSUMER_LATCH.await();
        Utils.getRestHighLevelClient().close();
        new CsvJob(Manager.RESULT_QUEUE).run();
        Manager.EXECUTOR_SERVICE.shutdown();
    }

    private static class Manager {
        private static final ConcurrentLinkedQueue<User> QUEUE = new ConcurrentLinkedQueue<>();
        private static final ConcurrentLinkedQueue<String> RESULT_QUEUE = new ConcurrentLinkedQueue<>();
        private static final AtomicLong SIZE = new AtomicLong(0);
        private static final Integer PRODUCER_COUNT = Integer.parseInt(Objects.toString(System.getProperty("producerCount"), "1"));
        private static final Integer CONSUMER_COUNT = Integer.parseInt(Objects.toString(System.getProperty("consumerCount"), "20"));
        private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(
                PRODUCER_COUNT + CONSUMER_COUNT,
                PRODUCER_COUNT + CONSUMER_COUNT,
                0,
                TimeUnit.MICROSECONDS,
                new ArrayBlockingQueue<>(100),
                Executors.defaultThreadFactory());
        private static final CountDownLatch PRODUCER_LATCH = new CountDownLatch(PRODUCER_COUNT);
        private static final CountDownLatch CONSUMER_LATCH = new CountDownLatch(CONSUMER_COUNT);
    }

    public static class ProducerJob implements Runnable {

        private final String appId;
        private final ConcurrentLinkedQueue<User> queue;
        private final AtomicLong size;
        private final Long maxSize;
        private final String idPrefix;
        private final String valuePrefix;

        public ProducerJob(ConcurrentLinkedQueue<User> queue, AtomicLong size) {
            this.appId = Objects.toString(System.getProperty("appId"), "");
            this.queue = queue;
            this.size = size;
            this.maxSize = Long.parseLong(System.getProperty("maxSize"));
            this.idPrefix = Objects.toString(System.getProperty("idPrefix"), "");
            this.valuePrefix = Objects.toString(System.getProperty("valuePrefix"), "");
        }

        @Override
        public void run() {
            long timestamp = System.currentTimeMillis();
            long tsSuffix = 1000;
            while (true) {
                long next = size.incrementAndGet();
                if (next > maxSize) {
                    break;
                }
                if (++tsSuffix == 9999) {
                    tsSuffix = 1000;
                    timestamp = System.currentTimeMillis();
                }
                long threadId = Thread.currentThread().getId();
                String uid = appId + "_" + idPrefix + "_" + threadId + "_" + timestamp + tsSuffix;
                queue.add(new User().setUserId(uid)
                        .setUserName("user_name_" + tsSuffix)
                        .setNickName(valuePrefix + "nick_name_" + tsSuffix)
                        .setSex("male")
                        .setAge(String.valueOf(tsSuffix))
                        .setCreateTime(System.currentTimeMillis())
                        .setUpdateTime(System.currentTimeMillis()));
            }
            Manager.PRODUCER_LATCH.countDown();
        }

    }

    public static class ConsumerJob implements Runnable {

        private final ConcurrentLinkedQueue<User> queue;
        private final AtomicLong size;
        private final Long maxSize;
        private final ConcurrentLinkedQueue<String> resultQueue;
        private final String indexName;
        private WriteRequest.RefreshPolicy refreshPolicy;

        public ConsumerJob(ConcurrentLinkedQueue<User> queue, AtomicLong size, ConcurrentLinkedQueue<String> resultQueue) {
            this.queue = queue;
            this.size = size;
            this.maxSize = Long.parseLong(System.getProperty("maxSize"));
            this.resultQueue = resultQueue;
            this.indexName = System.getProperty("indexName");
            String refreshPolicyStr = System.getProperty("refreshPolicy");
            if ("IMMEDIATE".equals(refreshPolicyStr)) {
                this.refreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE;
            } else {
                this.refreshPolicy = WriteRequest.RefreshPolicy.NONE;
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    User user = queue.poll();
                    if (user == null) {
                        if (size.get() > maxSize) {
                            break;
                        } else {
                            continue;
                        }
                    }
                    // 保存
                    long indexTs = System.currentTimeMillis();
                    String index = Utils.index(indexName, user.getUserId(), JSON.toJSONString(user), refreshPolicy);
                    if ("CREATED".equals(index)) {
                        // 查询
                        SearchHits searchHits = Utils.termQuery(indexName, "userId", user.getUserId());
                        long queryTs = System.currentTimeMillis();
                        // 输出
                        resultQueue.add(user.getUserId() + "," + indexTs + "," + queryTs + "," + searchHits.getTotalHits().value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Manager.CONSUMER_LATCH.countDown();
            }
        }

    }

    public static class CsvJob implements Runnable {

        private final ConcurrentLinkedQueue<String> resultQueue;
        private final String appId;

        public CsvJob(ConcurrentLinkedQueue<String> resultQueue) {
            this.resultQueue = resultQueue;
            this.appId = Objects.toString(System.getProperty("appId"), "");
        }

        @Override
        public void run() {
            try {
                String path = System.getProperty("user.home") + "\\Desktop\\TestRefreshPolicy";
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdir();
                }
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                path = path + "\\" + appId + "_" + Thread.currentThread().getName() + "_" + dateFormat.format(new Date()) + ".csv";
                file = new File(path);
                if (!file.exists()) {
                    file.createNewFile();
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                while (true) {
                    String ele = resultQueue.poll();
                    if (ele == null) {
                        if (Manager.CONSUMER_LATCH.getCount() == 0) {
                            break;
                        } else {
                            continue;
                        }
                    }
                    writer.write(ele);
                    writer.newLine();
                }
                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Data
    @Accessors(chain = true)
    public static class User {
        private String userId;
        private String userName;
        private String nickName;
        private String sex;
        private String age;
        private Long createTime;
        private Long updateTime;
    }

}
