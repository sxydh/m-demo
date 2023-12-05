package cn.net.bhe.phoenixclientdemo;

import cn.net.bhe.mutil.StrUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class _Main {

    public static void main(String[] args) throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:phoenix:hadoop01,hadoop02,hadoop03:2181");
        PreparedStatement preparedStatement = connection.prepareStatement("select * from app_log");

        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        while (resultSet.next()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 1, j = metaData.getColumnCount(); i <= metaData.getColumnCount(); i++) {
                builder.append(resultSet.getObject(i));
                if (i != j) {
                    builder.append(StrUtils.COMMA);
                }
            }
            log.info(builder.toString());
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();
    }

}
