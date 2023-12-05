package cn.net.bhe.hbaseclientdemo.dmldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class GetDemo {

    public Result get(String ns, String tb, String rowKey, String colg, String col) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Table table = connection.getTable(TableName.valueOf(ns, tb));

        Get get = new Get(Bytes.toBytes(rowKey));
        get.addColumn(Bytes.toBytes(colg), Bytes.toBytes(col));
        get.readAllVersions();
        Result row = table.get(get);

        CreateConnDemo.closeConn(table);
        return row;
    }

}
