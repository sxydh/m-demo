package cn.net.bhe.hbaseclientdemo.conndemo;

import cn.net.bhe.mutil.ArrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.Closeable;

@Slf4j
public class CreateConnDemo {

    private static Connection conn;
    private static final Object CONN_LOCK = new Object();

    public static Connection createConn() {
        if (conn != null) {
            return conn;
        }
        synchronized (CONN_LOCK) {
            if (conn != null) {
                return conn;
            }
            try {
                /* 创建配置 */
                Configuration conf = new Configuration();
                conf.set("hbase.zookeeper.quorum", "hadoop01,hadoop02,hadoop03");
                /* 创建连接 */
                conn = ConnectionFactory.createConnection(conf);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
            return conn;
        }
    }

    public static void closeConn(Closeable... closeables) {
        synchronized (CONN_LOCK) {
            if (ArrUtils.isNotEmpty(closeables)) {
                for (Closeable closeable : closeables) {
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        log.warn(e.getLocalizedMessage());
                    }
                }
            }
            if (conn != null && !conn.isClosed()) {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.warn(e.getLocalizedMessage());
                }
            }
        }
    }

}
