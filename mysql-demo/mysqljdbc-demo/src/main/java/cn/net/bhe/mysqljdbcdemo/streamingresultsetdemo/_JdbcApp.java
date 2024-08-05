package cn.net.bhe.mysqljdbcdemo.streamingresultsetdemo;

import cn.net.bhe.mutil.RsUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class _JdbcApp {

    public static void main(String[] args) throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://192.168.233.129:3306/pet_demo", "root", "123")) {
            PreparedStatement preparedStatement = connection.prepareStatement(" select * from t_order limit 200000 ", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // 需要显示设置 fetchSize 为 Integer.MIN_VALUE，否则不是流式结果集。
            // 流式结果集的条件源码 com.mysql.cj.jdbc.StatementImpl.createStreamingResultSet
            // 非流式结果集查询大量数据时可能会造成 OOM，模拟 OOM 需要预先设置 -Xms8m -Xmx8m。
            preparedStatement.setFetchSize(Integer.MIN_VALUE);
            long start = System.currentTimeMillis();
            ResultSet resultSet = preparedStatement.executeQuery();
            RsUtils.RsIt rsIt = RsUtils.getRsIt(resultSet);
            int size = 0;
            while (rsIt.hasNext()) {
                size++;
            }
            System.out.println("size = " + size + ", mills = " + (System.currentTimeMillis() - start));
        }
    }

}
