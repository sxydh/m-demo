package cn.net.bhe.hbaseclientdemo.ddldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;

public class TableExistsDemo {

    public boolean tableExists(String ns, String tb) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Admin admin = connection.getAdmin();

        boolean exists = admin.tableExists(TableName.valueOf(ns, tb));

        CreateConnDemo.closeConn(admin);
        return exists;
    }

}
