package cn.net.bhe.erhlcdemo;

public interface ErhlcService {

    boolean createIndex(String indexName) throws Exception;

    String[] getIndex(String indexName) throws Exception;

    boolean delIndex(String indexName) throws Exception;

    String createDoc(String indexName, String id, String doc) throws Exception;

    String getDoc(String indexName, String id) throws Exception;

    String delDoc(String indexName, String id) throws Exception;

    String[] bulkDoc(String indexName, String[] ids, String[] docs) throws Exception;

    String[] delBulkDoc(String indexName, String[] ids) throws Exception;

    String[] query(String indexName, String sort, int from, int size) throws Exception;

}
