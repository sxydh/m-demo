package cn.net.bhe.sparksqldemo.outputdemo

import cn.net.bhe.sparksqldemo.{logInput, logOutput}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

//noinspection DuplicatedCode
object JsonFileDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 数据输入
    val df: DataFrame = spark.read.json(logInput)

    // 数据输出
    df.write.json(logOutput)

    // 释放资源
    spark.close()
  }

}
