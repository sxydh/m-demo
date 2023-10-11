package cn.net.bhe;

import com.alibaba.fastjson2.JSONObject;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Hello world!
 */
public class App {
    static RestHighLevelClient restHighLevelClient;
    static {
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        restHighLevelClient = new RestHighLevelClient(builder);
    }
    static ExecutorService executorService = Executors.newFixedThreadPool(100);
    ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    static ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(100000);
    static File file = new File("C:/Users/Administrator/Desktop/es.csv");
    static BufferedWriter writer;
    static {
        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        GetAliasesRequest request = new GetAliasesRequest();
        GetAliasesResponse alias = restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
        Map<String, Set<AliasMetadata>> aliases = alias.getAliases();
        System.out.println(aliases);
    }

    public void createIndex() throws Exception {
        CreateIndexRequest request = new CreateIndexRequest("test_index01");
        CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response.index());
        System.out.println(response.isAcknowledged());
    }

    public void createDoc() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", UUID.randomUUID().toString());
        jsonObject.put("name", "Jack");
        jsonObject.put("age", 18);

        IndexRequest request = new IndexRequest("test_index01");
        request.id(jsonObject.getString("id"));
        request.timeout(TimeValue.timeValueSeconds(1));
        request.source(jsonObject.toJSONString(), XContentType.JSON);

        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(index.toString());
    }

    public void createDocByMulti() throws Exception {
        for (int i = 0; i < 50; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < 100000; j++) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", UUID.randomUUID().toString() + j);
                        jsonObject.put("name", UUID.randomUUID().toString());
                        jsonObject.put("age", new Random().nextInt());
                        jsonObject.put("timestamp", System.currentTimeMillis());

                        IndexRequest request = new IndexRequest("test_index01");
                        request.id(jsonObject.getString("id"));
                        request.timeout(TimeValue.timeValueSeconds(1));
                        request.source(jsonObject.toJSONString(), XContentType.JSON);
//                        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

                        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
                        map.put(jsonObject.getString("id"), jsonObject.getString("timestamp"));
                        getDoc(jsonObject.getString("id"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        boolean b = executorService.awaitTermination(10, TimeUnit.SECONDS);
        for (String value : map.values()) {
            writer.write(value);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public void getDoc(String id) throws Exception {
        GetRequest request = new GetRequest("test_index01", id);
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        map.put(id, map.get(id) + "," + System.currentTimeMillis() + "," + response.isExists());
        System.out.println(map);
    }

    public void countDoc() throws Exception {
        CountRequest request = new CountRequest();
        request.indices("test_index01");
        CountResponse count = restHighLevelClient.count(request, RequestOptions.DEFAULT);
        System.out.println(count.getCount());
    }

    public BufferedWriter writeCsv(String value) throws Exception {
        writer.write(value);
        return writer;
    }

}
