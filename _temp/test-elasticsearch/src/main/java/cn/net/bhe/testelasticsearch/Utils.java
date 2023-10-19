package cn.net.bhe.testelasticsearch;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utils
 *
 * @author sxydh
 * @since 1.0
 */
public class Utils {

    private static final Object getEsClientLock = new Object();
    private static RestHighLevelClient restHighLevelClient;

    public static RestHighLevelClient getRestHighLevelClient() {
        synchronized (getEsClientLock) {
            if (restHighLevelClient == null) {
                RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
                restHighLevelClient = new RestHighLevelClient(builder);
            }
            return restHighLevelClient;
        }
    }

    public static boolean createIndex(String indexName, Map<String, ?> settings, Map<String, Object> mapping) throws Exception {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(settings);
        if (ObjectUtils.isNotEmpty(mapping)) {
            request.mapping(mapping);
        }
        CreateIndexResponse createIndexResponse = getRestHighLevelClient().indices().create(request, RequestOptions.DEFAULT);
        return createIndexResponse.isAcknowledged();
    }

    public static GetIndexResponse getIndex(String indexName) throws Exception {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return getRestHighLevelClient().indices().get(request, RequestOptions.DEFAULT);
    }

    public static boolean indexExists(String indexName) throws Exception {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return getRestHighLevelClient().indices().exists(request, RequestOptions.DEFAULT);
    }

    public static boolean deleteIndex(String indexName) throws Exception {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        return getRestHighLevelClient().indices().delete(request, RequestOptions.DEFAULT).isAcknowledged();
    }

    public static boolean putMapping(String indexName, Map<String, Object> source) throws Exception {
        PutMappingRequest request = new PutMappingRequest(indexName);
        request.source(source);
        return getRestHighLevelClient().indices().putMapping(request, RequestOptions.DEFAULT).isAcknowledged();
    }

    public static String index(String indexName, String id, String source, WriteRequest.RefreshPolicy refreshPolicy) throws Exception {
        IndexRequest request = new IndexRequest(indexName);
        request.id(id);
        request.source(source, XContentType.JSON);
        request.setRefreshPolicy(refreshPolicy);
        IndexResponse index = getRestHighLevelClient().index(request, RequestOptions.DEFAULT);
        return index.getResult().name();
    }

    public static String bulk(String indexName, List<String> idList, List<String> sourceList, WriteRequest.RefreshPolicy refreshPolicy) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < idList.size(); i++) {
            IndexRequest indexRequest = new IndexRequest(indexName);
            indexRequest.id(idList.get(i));
            indexRequest.source(sourceList.get(i), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        bulkRequest.setRefreshPolicy(refreshPolicy);
        BulkResponse bulk = getRestHighLevelClient().bulk(bulkRequest, RequestOptions.DEFAULT);
        return Arrays.toString(Arrays.stream(bulk.getItems()).map(e -> e.getResponse().getResult().name()).toArray(String[]::new));
    }

    public static String delete(String indexName, String id) throws Exception {
        DeleteRequest request = new DeleteRequest(indexName);
        request.id(id);
        DeleteResponse delete = getRestHighLevelClient().delete(request, RequestOptions.DEFAULT);
        return delete.getResult().name();
    }

    public static SearchHits termQuery(String indexName, String name, String value) throws Exception {
        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(0);
        sourceBuilder.size(5);
        sourceBuilder.query(QueryBuilders.termQuery(name, value));
        request.source(sourceBuilder);
        SearchResponse search = getRestHighLevelClient().search(request, RequestOptions.DEFAULT);
        return search.getHits();
    }

}
