package cn.net.bhe.kotlindbcdemo.sqlite_demo

import cn.net.bhe.mutil.StrUtils
import com.alibaba.fastjson2.JSONObject
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object JsonHelper {

    fun read2JsonList(resultSet: ResultSet): List<JSONObject> {
        resultSet.use {
            val columns: MutableList<String> = ArrayList()
            val metaData = resultSet.metaData
            for (i in 1..metaData.columnCount) {
                columns.add(metaData.getColumnLabel(i))
            }
            val jsonList: MutableList<JSONObject> = ArrayList()
            while (resultSet.next()) {
                val json = JSONObject()
                for (column in columns) {
                    json[column] = resultSet.getObject(column)?.toString() ?: StrUtils.EMPTY
                }
                jsonList.add(json)
            }
            return jsonList
        }
    }

}

class Conn(url: String) {
    private val conn: Connection = DriverManager.getConnection(url)

    fun get(): Connection {
        return conn
    }
}


class DbHelper(private val conn: Conn) {

    fun exec(sql: String): Boolean {
        return conn.get().createStatement().execute(sql)
    }

    fun execQuery(sql: String): List<JSONObject> {
        val resultSet = conn.get().createStatement().executeQuery(sql)
        return JsonHelper.read2JsonList(resultSet)
    }

}

fun main() {
}