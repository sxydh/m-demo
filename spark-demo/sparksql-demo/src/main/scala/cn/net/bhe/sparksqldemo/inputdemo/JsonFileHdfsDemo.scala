package cn.net.bhe.sparksqldemo.inputdemo

import cn.net.bhe.sparksqldemo.{logHdfsInput, logInput}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

//noinspection DuplicatedCode
object JsonFileHdfsDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 数据输入
    val df: DataFrame = spark.read.json(logHdfsInput)

    // 数据处理
    df.select("*").show()

    // 释放资源
    spark.close()
  }

}
