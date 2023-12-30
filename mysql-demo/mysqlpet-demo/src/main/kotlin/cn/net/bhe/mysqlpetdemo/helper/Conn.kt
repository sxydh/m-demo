package cn.net.bhe.mysqlpetdemo.helper

import java.sql.Connection
import java.sql.DriverManager

fun getConn(): Connection {
    return DriverManager.getConnection("jdbc:mysql://localhost:3306/pet_demo", "root", "123")
}