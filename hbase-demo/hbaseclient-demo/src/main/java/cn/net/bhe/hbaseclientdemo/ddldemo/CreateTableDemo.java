package cn.net.bhe.hbaseclientdemo.ddldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;

public class CreateTableDemo {

    public void createTable(String ns, String tb, String... colgs) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Admin admin = connection.getAdmin();

        TableDescriptor tableDescriptor = getTableDescriptor(ns, tb, colgs);
        admin.createTable(tableDescriptor);

        CreateConnDemo.closeConn(admin);
    }

    public void modifyTable(String ns, String tb, String... colgs) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Admin admin = connection.getAdmin();

        TableDescriptor tableDescriptor = getTableDescriptor(ns, tb, colgs);
        admin.modifyTable(tableDescriptor);

        CreateConnDemo.closeConn(admin);
    }

    private TableDescriptor getTableDescriptor(String ns, String tb, String... colgs) {
        // HDFS路径：/hbase/data/<namespace>/<table>。
        TableDescriptorBuilder tableBuilder = TableDescriptorBuilder.newBuilder(TableName.valueOf(ns, tb));
        // HDFS路径：/hbase/data/<namespace>/<table>/<regionId>/<columnFamily>
        List<ColumnFamilyDescriptor> familyDescriptorList = new ArrayList<>();
        for (String colg : colgs) {
            familyDescriptorList.add(ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(colg)).setMaxVersions(5).build());
        }
        tableBuilder.setColumnFamilies(familyDescriptorList);
        return tableBuilder.build();
    }

}
