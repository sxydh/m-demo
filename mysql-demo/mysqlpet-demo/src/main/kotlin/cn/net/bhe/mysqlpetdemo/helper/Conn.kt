package cn.net.bhe.mysqlpetdemo.helper

import java.sql.Connection
import java.sql.DriverManager

fun getConn(): Connection {
    return DriverManager.getConnection("jdbc:mysql://192.168.233.135:3306/pet_demo", "root", "123")
}