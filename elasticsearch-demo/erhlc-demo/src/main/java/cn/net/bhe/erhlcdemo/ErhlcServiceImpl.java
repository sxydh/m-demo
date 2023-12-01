package cn.net.bhe.erhlcdemo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Arrays;

public class ErhlcServiceImpl implements ErhlcService {

    @Override
    public boolean createIndex(String indexName) throws Exception {
        RestHighLevelClient client = ErhlcHelper.getClient();
        CreateIndexResponse createIndexResponse = client.indices().create(new CreateIndexRequest(indexName), RequestOptions.DEFAULT);
        return createIndexResponse.isAcknowledged();
    }

    @Override
    public String[] getIndex(String indexName) throws Exception {
        RestHighLevelClient client = ErhlcHelper.getClient();
        GetIndexResponse getIndexResponse = client.indices().get(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
        String[] ret = new String[3];
        ret[0] = Arrays.toString(getIndexResponse.getIndices());
        ret[1] = Arrays.toString(getIndexResponse.getSettings().values().stream().map(Settings::toString).toArray());
        ret[2] = Arrays.toString(getIndexResponse.getMappings().values().stream().map(MappingMetadata::getSourceAsMap).toArray());
        return ret;
    }

    @Override
    public boolean delIndex(String indexName) throws Exception {
        RestHighLevelClient client = ErhlcHelper.getClient();
        AcknowledgedResponse ret = client.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
        return ret.isAcknowledged();
    }

    @Override
    public String createDoc(String indexName, String id, String doc) throws Exception {
        RestHighLevelClient client = ErhlcHelper.getClient();
        IndexRequest request = new IndexRequest();
        request.index(indexName).id(id);
        request.source(doc, XContentType.JSON);
        IndexResponse ret = client.index(request, RequestOptions.DEFAULT);
        return ret.getResult().name();
    }

    @Override
    public String getDoc(String indexName, String id) throws Exception {
        GetRequest request = new GetRequest();
        request.index(indexName).id(id);
        RestHighLevelClient client = ErhlcHelper.getClient();
        GetResponse ret = client.get(request, RequestOptions.DEFAULT);
        return ret.getSourceAsString();
    }

    @Override
    public String delDoc(String indexName, String id) throws Exception {
        DeleteRequest request = new DeleteRequest();
        request.index(indexName).id(id);
        RestHighLevelClient client = ErhlcHelper.getClient();
        DeleteResponse ret = client.delete(request, RequestOptions.DEFAULT);
        return ret.getResult().name();
    }

    @Override
    public String[] bulkDoc(String indexName, String[] ids, String[] docs) throws Exception {
        BulkRequest request = new BulkRequest();
        for (int i = 0; i < ids.length; i++) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.index(indexName).id(ids[i]);
            indexRequest.source(docs[i], XContentType.JSON);
            request.add(indexRequest);
        }
        RestHighLevelClient client = ErhlcHelper.getClient();
        BulkResponse ret = client.bulk(request, RequestOptions.DEFAULT);
        return Arrays.stream(ret.getItems()).map(e -> e.getResponse().getResult().name()).toArray(String[]::new);
    }

    @Override
    public String[] delBulkDoc(String indexName, String[] ids) throws Exception {
        BulkRequest request = new BulkRequest();
        for (String id : ids) {
            DeleteRequest deleteRequest = new DeleteRequest();
            deleteRequest.index(indexName).id(id);
            request.add(deleteRequest);
        }
        RestHighLevelClient client = ErhlcHelper.getClient();
        BulkResponse ret = client.bulk(request, RequestOptions.DEFAULT);
        return Arrays.stream(ret.getItems()).map(e -> e.getResponse().getResult().name()).toArray(String[]::new);
    }

    @Override
    public String[] query(String indexName, String sort, int from, int size) throws Exception {
        SearchRequest request = new SearchRequest();
        request.indices(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
        sourceBuilder.from(from);
        sourceBuilder.size(size);
        sourceBuilder.sort(sort, SortOrder.ASC);
        request.source(sourceBuilder);
        RestHighLevelClient client = ErhlcHelper.getClient();
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        String[] ret = new String[2];
        ret[0] = String.valueOf(searchResponse.getHits().getTotalHits().value);
        ret[1] = Arrays.toString(Arrays.stream(searchResponse.getHits().getHits()).map(SearchHit::getSourceAsString).toArray());
        return ret;
    }

    @Data
    @Accessors(chain = true)
    public static class User {
        private Long id;
        private String name;
        private Integer age;
        private String sex;
    }

}
