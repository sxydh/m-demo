package cn.net.bhe.hbaseclientdemo.dmldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import cn.net.bhe.mutil.Snowflake;
import cn.net.bhe.mutil.StrUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;

public class PutDemo {

    public void put(String ns, String tb, String cf) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Table table = connection.getTable(TableName.valueOf(ns, tb));

        // HDFS路径：/hbase/data/<namespace>/<table>/<regionId>/<columnFamily>/<fileName>
        List<Put> rowList = new ArrayList<>();
        Snowflake snowflake = new Snowflake();
        for (int i = 1; i <= 10000; i++) {
            for (int j = 1; j <= 500; j++) {
                String rowKey = "row" + snowflake.nextId();
                Put row = new Put(Bytes.toBytes(rowKey));
                for (int k = 1; k <= 4; k++) {
                    String colQualifier = "col" + k;
                    String value = StrUtils.randomChs(20);
                    row.addColumn(Bytes.toBytes(cf), Bytes.toBytes(colQualifier), Bytes.toBytes(value));
                }
                rowList.add(row);
            }
            table.put(rowList);
            rowList.clear();
        }

        CreateConnDemo.closeConn(table);
    }

}
