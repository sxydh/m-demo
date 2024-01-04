package cn.net.bhe.espetdemo.insert;

import cn.net.bhe.espetdemo.helper.DataHelper;
import cn.net.bhe.espetdemo.helper.DataHelper.Order;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

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
        int n = 100000;
        int threads = 1;
        int allocSec = 3600;
        boolean isBulk = true;
        String refreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE.getValue();
        String index = "pet_order";
        String[] hosts = new String[]{"192.168.233.129", "192.168.233.130", "192.168.233.131"};
        int port = 9200;

        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            httpHosts[i] = new HttpHost(hosts[i], port, "http");
        }
        // noinspection resource
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(httpHosts));

        List<String> files = DataHelper.create(n, threads, allocSec);
        ExecutorService executorService = Executors.newFixedThreadPool(files.size());
        for (String file : files) {
            executorService.submit(() -> {
                try {
                    long mills = System.currentTimeMillis();

                    Map<String, Object> res = doInsert(client, index, file, isBulk, refreshPolicy);

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

    private static Map<String, Object> doInsert(RestHighLevelClient client, String index, String file, boolean isBulk, String refreshPolicy) throws Exception {
        try (FileReader fileReader = new FileReader(file)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                int size = 0;
                Map<Long, Integer> rtMap = new HashMap<>();
                List<Order> bulk = new ArrayList<>();
                while (true) {
                    long start = System.currentTimeMillis();
                    String line = bufferedReader.readLine();
                    if (line != null) {

                        Order order = JSON.parseObject(line, Order.class);
                        if (isBulk) {
                            bulk.add(order);
                            if (size != 0 && bulk.size() % 500 == 0) {
                                doInsert(client, index, bulk, refreshPolicy);
                                bulk.clear();
                            }
                        } else {
                            doInsert(client, index, order, refreshPolicy);
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
                        if (isBulk && !bulk.isEmpty()) {
                            doInsert(client, index, bulk, refreshPolicy);
                            bulk.clear();
                        }
                        break;
                    }
                }
                Map<String, Object> res = new HashMap<>();
                res.put("size", size);
                res.put("rtMap", rtMap);
                return res;
            }
        }
    }

    private static void doInsert(RestHighLevelClient client, String index, Order order, String refreshPolicy) throws Exception {
        IndexRequest request = new IndexRequest();
        request.index(index).id(order.getOrderId().toString());
        request.source(JSON.toJSONString(order), XContentType.JSON);
        request.setRefreshPolicy(refreshPolicy);
        client.index(request, RequestOptions.DEFAULT);
    }

    private static void doInsert(RestHighLevelClient client, String index, List<Order> orders, String refreshPolicy) throws Exception {
        BulkRequest request = new BulkRequest();
        for (Order order : orders) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.index(index).id(order.getOrderId().toString());
            indexRequest.source(JSON.toJSONString(order), XContentType.JSON);
            request.add(indexRequest);
        }
        request.setRefreshPolicy(refreshPolicy);
        client.bulk(request, RequestOptions.DEFAULT);
    }

}
