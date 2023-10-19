package cn.net.bhe.testelasticsearch;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

class UtilsTest {

    private static String indexName;
    private static Map<String, Object> settings;
    private static Map<String, Object> mapping;

    @BeforeAll
    static void beforeEach() {
        indexName = "test_index01";

        settings = new HashMap<>(16);
        settings.put("index.number_of_shards", "4");
        settings.put("index.number_of_replicas", "2");

        mapping = new HashMap<>(16);
        Map<String, Object> properties = new HashMap<>(16);
        properties.put("userId", JSONObject.of("type", "keyword", "index", "true"));
        properties.put("userName", JSONObject.of("type", "text", "index", "true"));
        properties.put("nickName", JSONObject.of("type", "text", "index", "true"));
        properties.put("sex", JSONObject.of("type", "keyword", "index", "true"));
        properties.put("age", JSONObject.of("type", "keyword", "index", "true"));
        properties.put("createTime", JSONObject.of("type", "date", "index", "true"));
        properties.put("updateTime", JSONObject.of("type", "date", "index", "true"));
        mapping.put("properties", properties);
    }

    @Test
    void createIndex() {
        try {
            boolean testIndex01 = Utils.createIndex(indexName, settings, mapping);
            System.out.println(testIndex01);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void getIndex() {
        try {
            GetIndexResponse testIndex01 = Utils.getIndex(indexName);
            System.out.println(Arrays.toString(testIndex01.getIndices()));
            System.out.println(Arrays.toString(testIndex01.getSettings().values().stream().map(Settings::toString).map(Object::toString).toArray()));
            System.out.println(Arrays.toString(testIndex01.getMappings().values().stream().map(MappingMetadata::getSourceAsMap).map(Object::toString).toArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void indexExists() {
        try {
            boolean testIndex01 = Utils.indexExists(indexName);
            System.out.println(testIndex01);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void deleteIndex() {
        try {
            boolean testIndex01 = Utils.deleteIndex(indexName);
            System.out.println(testIndex01);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void putMapping() {
        try {
            boolean b = Utils.putMapping(indexName, mapping);
            System.out.println(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void index() {
        try {
            TestRefreshPolicy.User user = new TestRefreshPolicy.User()
                    .setUserId("1")
                    .setUserName("30053631")
                    .setNickName("SuHeng")
                    .setSex("male")
                    .setAge("32")
                    .setCreateTime(System.currentTimeMillis())
                    .setUpdateTime(System.currentTimeMillis());
            String index = Utils.index(indexName, user.getUserId(), JSON.toJSONString(user), WriteRequest.RefreshPolicy.NONE);
            System.out.println(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void bulk() {
        try {
            List<String> idList = new ArrayList<>();
            List<String> sourceList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                idList.add("bulk_id_" + i);
                TestRefreshPolicy.User user = new TestRefreshPolicy.User()
                        .setUserId("bulk_user_id_" + i)
                        .setUserName("bulk_user_name_" + i)
                        .setNickName("bulk_nick_name_" + i)
                        .setSex("male")
                        .setAge(String.valueOf(20 + new Random().nextInt(10)))
                        .setCreateTime(System.currentTimeMillis())
                        .setUpdateTime(System.currentTimeMillis());
                sourceList.add(JSON.toJSONString(user));
            }
            String testIndex01 = Utils.bulk(indexName, idList, sourceList, WriteRequest.RefreshPolicy.NONE);
            System.out.println(testIndex01);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void delete() {
        try {
            String testIndex01 = Utils.delete(indexName, "1");
            System.out.println(testIndex01);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void termQuery() {
        try {
            SearchHits searchHits = Utils.termQuery(indexName, "nickName", "bulk_nick_name_1");
            System.out.println(searchHits.getTotalHits().value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}