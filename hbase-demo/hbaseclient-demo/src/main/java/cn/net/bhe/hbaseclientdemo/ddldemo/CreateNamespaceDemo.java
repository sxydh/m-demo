package cn.net.bhe.hbaseclientdemo.ddldemo;

import cn.net.bhe.hbaseclientdemo.conndemo.CreateConnDemo;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;

public class CreateNamespaceDemo {

    public void createNamespace(String namespace) throws Exception {
        Connection connection = CreateConnDemo.createConn();
        Admin admin = connection.getAdmin();

        // HDFS存储路径：/hbase/data/<namespace>。
        NamespaceDescriptor.Builder ndBuilder = NamespaceDescriptor.create(namespace);
        ndBuilder.addConfiguration("user", "sxydh");
        admin.createNamespace(ndBuilder.build());

        CreateConnDemo.closeConn(admin);
    }

}
