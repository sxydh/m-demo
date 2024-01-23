package cn.net.bhe.sparksqldemo.inputdemo

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util.Properties

//noinspection DuplicatedCode
object JdbcDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 数据输入
    val properties = new Properties()
    properties.setProperty("user", "root")
    properties.setProperty("password", "123")
    properties.setProperty("driver", "com.mysql.cj.jdbc.Driver")
    val df: DataFrame = spark.read.jdbc(
      url = "jdbc:mysql://192.168.233.129:3306/sparksql_demo",
      table = "t_log",
      predicates = Array(" value < 5 "),
      connectionProperties = properties)

    // 数据处理
    println(df.count())

    // 释放资源
    spark.close()
  }

}
