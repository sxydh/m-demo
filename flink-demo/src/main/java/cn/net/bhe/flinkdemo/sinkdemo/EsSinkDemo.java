package cn.net.bhe.flinkdemo.sinkdemo;

import cn.net.bhe.mutil.LiUtils;
import cn.net.bhe.mutil.Snowflake;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsSinkDemo {

    public static void main(String[] args) throws Exception {
        new EsSinkDemo().wordSave();
    }

    public void wordSave() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据出口 */
        // 数据保存到ES集群
        dataStream.addSink(getEsSink());

        /* 执行 */
        environment.execute();
    }

    private static ElasticsearchSink<String> getEsSink() {
        List<HttpHost> hostList = LiUtils.of(
                new HttpHost("192.168.233.129", 9200, "http"),
                new HttpHost("192.168.233.130", 9200, "http"),
                new HttpHost("192.168.233.131", 9200, "http"));
        ElasticsearchSink.Builder<String> esSink = new ElasticsearchSink.Builder<>(hostList, new WordElasticsearchSinkFunction());
        // ES配置：每1000条数据或者每2秒提交一次。
        esSink.setBulkFlushMaxActions(1000);
        esSink.setBulkFlushInterval(2000);
        return esSink.build();
    }

    public static class WordElasticsearchSinkFunction implements ElasticsearchSinkFunction<String> {

        @Override
        public void process(String element, RuntimeContext ctx, RequestIndexer indexer) {
            String id = String.valueOf(new Snowflake().nextId());
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("value", element);

            IndexRequest request = Requests.indexRequest()
                    .index("shopping")
                    .id(id)
                    .source(map);

            indexer.add(request);
        }
    }

}
