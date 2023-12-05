package cn.net.bhe.hbaseclientdemo.dmldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import cn.net.bhe.mutil.StrUtils;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.ColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class FilterDemo {

    public ResultScanner filter(String ns, String tb, String startRow, String endRow, String colg, String col, String equal) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Table table = connection.getTable(TableName.valueOf(ns, tb));

        Scan scan = new Scan();
        if (StrUtils.isNotEmpty(startRow)) {
            scan.withStartRow(Bytes.toBytes(startRow));
        }
        if (StrUtils.isNotEmpty(endRow)) {
            scan.withStopRow(Bytes.toBytes(endRow));
        }
        ColumnValueFilter filter = new ColumnValueFilter(Bytes.toBytes(colg), Bytes.toBytes(col), CompareOperator.EQUAL, Bytes.toBytes(equal));
        scan.setFilter(filter);

        return table.getScanner(scan);
    }

}
