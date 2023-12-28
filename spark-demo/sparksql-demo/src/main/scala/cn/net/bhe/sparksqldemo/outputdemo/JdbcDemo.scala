package cn.net.bhe.sparksqldemo.outputdemo

import cn.net.bhe.sparksqldemo.logInput
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

//noinspection DuplicatedCode
object JdbcDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 数据输入
    val df: DataFrame = spark.read.json(logInput)

    // 数据输出
    val url = "jdbc:mysql://192.168.233.129:3306/sparksql_demo"
    val properties = new java.util.Properties()
    properties.setProperty("user", "root")
    properties.setProperty("password", "123")
    df.write.jdbc(url, "t_log", properties)

    // 释放资源
    spark.close()
  }

}
