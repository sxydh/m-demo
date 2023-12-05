package cn.net.bhe.hivejdbcdemo;

import cn.net.bhe.mutil.Snowflake;
import cn.net.bhe.mutil.StrUtils;

import java.sql.*;
import java.util.Random;

public class _Main {

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    public static void main(String[] args) throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:hive2://hadoop01:10000/default", "sxydh", "1");
        Statement statement = connection.createStatement();
        statement.execute(" create database if not exists testdb ");
        statement.execute(" create table if not exists testdb.app_log(log_id string, ip string, op string, val string) "
                + " comment 'App操作日志' "
                + " row format delimited "
                + " fields terminated by '\t' "
                + " lines terminated by '\n' "
                + " stored as textfile ");
        PreparedStatement preparedStatement = connection.prepareStatement(" insert into testdb.app_log(log_id, ip, op, val) values(?, ?, ? ,?) ");
        Snowflake snowflake = new Snowflake();
        Random random = new Random();
        for (int i = 0; i < 1; i++) {
            preparedStatement.setString(1, String.valueOf(snowflake.nextId()));
            preparedStatement.setString(2, random.nextInt(255) + StrUtils.DOT + random.nextInt(255) + StrUtils.DOT + random.nextInt(255) + StrUtils.DOT + random.nextInt(255));
            preparedStatement.setString(3, new String[]{"INSERT", "UPDATE", "DELETE", "QUERY"}[random.nextInt(4)]);
            preparedStatement.setString(4, StrUtils.randomChs(20));
            preparedStatement.execute();
        }
        ResultSet resultSet = preparedStatement.executeQuery(" select * from testdb.app_log ");
        ResultSetMetaData metaData = resultSet.getMetaData();
        while (resultSet.next()) {
            for (int i = 1, j = metaData.getColumnCount(); i <= metaData.getColumnCount(); i++) {
                System.out.print(resultSet.getObject(i));
                if (i != j) {
                    System.out.print(StrUtils.COMMA);
                }
            }
            System.out.println();
        }
        resultSet.close();
        preparedStatement.close();
        connection.close();
    }

}
