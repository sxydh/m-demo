package cn.net.bhe.sparksqldemo.inputdemo

import cn.net.bhe.sparksqldemo.logSeq
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

//noinspection DuplicatedCode
object SeqDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 数据输入
    val rdd: RDD[(String, String, String, String)] = spark.sparkContext.parallelize(logSeq)
    import spark.implicits._
    val df = rdd.toDF("id", "ip", "op", "value")
    df.show()

    // 释放资源
    spark.close()
  }

}
