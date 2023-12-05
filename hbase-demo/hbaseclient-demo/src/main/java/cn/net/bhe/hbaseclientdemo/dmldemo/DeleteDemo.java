package cn.net.bhe.hbaseclientdemo.dmldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class DeleteDemo {

    public void delete(String ns, String tb, String rowKey, String colg, String col) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Table table = connection.getTable(TableName.valueOf(ns, tb));

        Delete delete = new Delete(Bytes.toBytes(rowKey));
        delete.addColumns(Bytes.toBytes(colg), Bytes.toBytes(col));
        table.delete(delete);

        CreateConnDemo.closeConn(table);
    }

}
