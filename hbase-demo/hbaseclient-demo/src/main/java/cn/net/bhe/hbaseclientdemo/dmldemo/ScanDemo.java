package cn.net.bhe.hbaseclientdemo.dmldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

@Slf4j
public class ScanDemo {

    public ResultScanner scan(String ns, String tb, String startRow, String endRow) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Table table = connection.getTable(TableName.valueOf(ns, tb));

        Scan scan = new Scan();
        scan.withStartRow(Bytes.toBytes(startRow));
        scan.withStopRow(Bytes.toBytes(endRow));
        return table.getScanner(scan);
    }

}
