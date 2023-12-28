package cn.net.bhe.sparksqldemo

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

//noinspection DuplicatedCode
object UdfDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 数据输入
    val df: DataFrame = spark.read.json(logInput)

    // 数据处理
    df.createOrReplaceTempView("log")
    spark.udf.register("tlc", (str: String) => {
      str.toLowerCase()
    })
    spark.sql(" select tlc(op) from log limit 5 ").show()

    // 释放资源
    spark.close()
  }

}
