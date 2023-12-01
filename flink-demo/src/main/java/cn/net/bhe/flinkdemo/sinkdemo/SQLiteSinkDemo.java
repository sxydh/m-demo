package cn.net.bhe.flinkdemo.sinkdemo;

import cn.net.bhe.mutil.As;
import cn.net.bhe.mutil.FlUtils;
import cn.net.bhe.mutil.Snowflake;
import cn.net.bhe.mutil.StrUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.io.File;
import java.sql.*;

public class SQLiteSinkDemo {

    public static void main(String[] args) throws Exception {
        new SQLiteSinkDemo().wordSave();
    }

    public void wordSave() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据出口 */
        // 数据保存到数据库SQLite
        dataStream.addSink(new SQLiteSinkFunction());

        /* 执行 */
        environment.execute();
    }

    private static final ThreadLocal<Connection> CONNECTION_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<PreparedStatement> INSERT_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<PreparedStatement> COUNT_THREAD_LOCAL = new ThreadLocal<>();
    private static final Snowflake SNOWFLAKE = new Snowflake();

    public static class SQLiteSinkFunction extends RichSinkFunction<String> {

        @Override
        public void open(Configuration parameters) throws Exception {
            if (CONNECTION_THREAD_LOCAL.get() == null) {
                String path = FlUtils.getRoot() + File.separator + "tmp";
                As.isTrue(FlUtils.mkdir(path));
                CONNECTION_THREAD_LOCAL.set(DriverManager.getConnection("jdbc:sqlite:" + path + File.separator + "word.db"));
                Statement statement = CONNECTION_THREAD_LOCAL.get().createStatement();
                statement.execute(" create table if not exists t_word (id text, val text) ");
                statement.close();
            }
            if (INSERT_THREAD_LOCAL.get() == null) {
                INSERT_THREAD_LOCAL.set(CONNECTION_THREAD_LOCAL.get().prepareStatement(" insert into t_word(id, val) values (?, ?) "));
            }
            if (COUNT_THREAD_LOCAL.get() == null) {
                COUNT_THREAD_LOCAL.set(CONNECTION_THREAD_LOCAL.get().prepareStatement(" select max(id), count(1) from t_word "));
            }
        }

        @Override
        public void invoke(String value, Context context) throws Exception {
            INSERT_THREAD_LOCAL.get().setString(1, String.valueOf(SNOWFLAKE.nextId()));
            INSERT_THREAD_LOCAL.get().setString(2, value);
            INSERT_THREAD_LOCAL.get().execute();
            try (ResultSet resultSet = COUNT_THREAD_LOCAL.get().executeQuery()) {
                System.out.println(resultSet.getString(1) + StrUtils.COMMA + resultSet.getString(2));
            }
        }

        @Override
        public void close() throws Exception {
            if (INSERT_THREAD_LOCAL.get() != null && !INSERT_THREAD_LOCAL.get().isClosed()) {
                INSERT_THREAD_LOCAL.get().close();
            }
            if (COUNT_THREAD_LOCAL.get() != null && !COUNT_THREAD_LOCAL.get().isClosed()) {
                COUNT_THREAD_LOCAL.get().close();
            }
            if (CONNECTION_THREAD_LOCAL.get() != null && !CONNECTION_THREAD_LOCAL.get().isClosed()) {
                CONNECTION_THREAD_LOCAL.get().close();
            }
            INSERT_THREAD_LOCAL.remove();
            COUNT_THREAD_LOCAL.remove();
            CONNECTION_THREAD_LOCAL.remove();
        }
    }

}
