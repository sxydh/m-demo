package cn.net.bhe.erhlcdemo;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class ErhlcHelper {

    private static final String[] HOSTS = new String[]{"192.168.233.129", "192.168.233.130", "192.168.233.131"};
    private static final int PORT = 9200;
    private static final String SCHEMA = "http";
    private static RestHighLevelClient client;

    private ErhlcHelper() {
    }

    public synchronized static RestHighLevelClient getClient() {
        if (client == null) {
            HttpHost[] httpHosts = new HttpHost[HOSTS.length];
            for (int i = 0; i < HOSTS.length; i++) {
                httpHosts[i] = new HttpHost(HOSTS[i], PORT, SCHEMA);
            }
            client = new RestHighLevelClient(RestClient.builder(httpHosts));
        }
        return client;
    }

}
