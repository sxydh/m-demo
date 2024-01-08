package cn.net.bhe.sparkcoredemo.sparklauncherdemo

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object SimpleJob {

  def main(args: Array[String]): Unit = {
    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    // 数据输入
    val rdd: RDD[String] = sc.parallelize(
      seq = Array("Hello Word!"),
      numSlices = 1)

    // 数据输出
    rdd.collect().foreach(println)

    // 释放资源
    sc.stop()
  }

}
