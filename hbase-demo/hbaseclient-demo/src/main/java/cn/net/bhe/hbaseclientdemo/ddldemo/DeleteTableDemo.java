package cn.net.bhe.hbaseclientdemo.ddldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;

public class DeleteTableDemo {

    public void deleteTable(String ns, String tb) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Admin admin = connection.getAdmin();

        admin.disableTable(TableName.valueOf(ns, tb));
        admin.deleteTable(TableName.valueOf(ns, tb));

        CreateConnDemo.closeConn(admin);
    }

}
