package cn.net.bhe.hbaseclientdemo.ddldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;

public class DeleteNamespaceDemo {

    public void deleteNamespace(String namespace) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Admin admin = connection.getAdmin();

        admin.deleteNamespace(namespace);

        CreateConnDemo.closeConn(admin);
    }

}
