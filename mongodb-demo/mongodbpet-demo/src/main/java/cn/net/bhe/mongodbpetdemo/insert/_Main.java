package cn.net.bhe.mongodbpetdemo.insert;

import cn.net.bhe.mongodbpetdemo.helper.DataHelper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class _Main {

    public static void main(String[] args) {
        int n = 10000;
        int threads = 1;
        int allocSec = 3600;
        boolean isMany = false;
        String conn = "mongodb://192.168.233.129:30000";

        List<String> files = DataHelper.create(n, threads, allocSec);
        ExecutorService executorService = Executors.newFixedThreadPool(files.size());
        for (String file : files) {
            executorService.submit(() -> {
                try (MongoClient mongoClient = MongoClients.create(conn)) {
                    MongoDatabase petDemo = mongoClient.getDatabase("pet_demo");
                    MongoCollection<Document> order = petDemo.getCollection("c_order");
                    long mills = System.currentTimeMillis();

                    Map<String, Object> res = insert(order, file, isMany);

                    mills = System.currentTimeMillis() - mills;
                    long size = (int) res.get("size");
                    int tps = (int) (size / (mills / 1000.0));
                    TreeMap<Long, BigDecimal> rtMap = new TreeMap<>();
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) res.get("rtMap")).entrySet()) {
                        int value = (int) entry.getValue();
                        BigDecimal calculatedValue = BigDecimal.valueOf(value * 1.0 / size * 100).setScale(4, RoundingMode.HALF_UP);
                        rtMap.put((Long) entry.getKey(), calculatedValue);
                    }
                    System.out.println("[" + Thread.currentThread().getName().trim() + "] " + "size = " + size + ", tps = " + tps + ", rtMap = " + rtMap);
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            });
        }
    }

    private static Map<String, Object> insert(MongoCollection<Document> collection, String file, boolean isMany) throws Exception {
        int size = 0;
        Map<Long, Integer> rtMap = new HashMap<>();
        List<Document> many = new ArrayList<>();
        try (FileReader fileReader = new FileReader(file)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                while (true) {
                    long start = System.currentTimeMillis();
                    String line = bufferedReader.readLine();
                    if (line != null) {

                        if (isMany) {
                            if (size != 0 && size % 200 == 0) {
                                collection.insertMany(many);
                                many.clear();
                            } else {
                                many.add(Document.parse(line));
                            }
                        } else {
                            collection.insertOne(Document.parse(line));
                        }

                        size++;
                        Long rt = (System.currentTimeMillis() - start) / 10 * 10;
                        Integer rtCount = rtMap.get(rt);
                        if (rtCount == null) {
                            rtCount = 1;
                        } else {
                            rtCount++;
                        }
                        rtMap.put(rt, rtCount);
                    } else {
                        if (isMany && !many.isEmpty()) {
                            collection.insertMany(many);
                            many.clear();
                        }
                        break;
                    }
                }
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("size", size);
        res.put("rtMap", rtMap);
        return res;
    }

}
