package cn.net.bhe.sparkhivedemo

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object _Main {

  def main(args: Array[String]): Unit = {

    // 环境准备
    System.setProperty("HADOOP_USER_NAME", "sxydh")
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val spark: SparkSession = SparkSession.builder().enableHiveSupport().config(sparkConf).getOrCreate()

    // 数据输入
    val df: DataFrame = spark.read.json(logInput)

    // 数据处理
    df.createOrReplaceTempView("log")
    val databases: Array[String] = spark.sql(" show databases ").collect().map(_.getString(0))
    if (!databases.contains("sh_demo")) {
      spark.sql(" create database sh_demo ")
    }
    spark.sql(" use sh_demo ")
    // 表存在时日志会输出异常
    // 日志配置已经调整为 log4j.rootLogger=WARN,logfile
    spark.sql(" create table if not exists t_log(id varchar(255), ip varchar(255), op varchar(255), value varchar(255)) ")
    for (it <- 1 to 100) {
      spark.sql(" insert into t_log select * from log ")
    }
    spark.sql(" select count(1) from t_log ").show()

    // 释放资源
    spark.close()
  }

}
