package cn.net.bhe.sparkcoredemo.transdemo

import cn.net.bhe.sparkcoredemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object SubtractDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    // 数据输入
    val rdd: RDD[Int] = sc.parallelize(
      seq = intSeq,
      numSlices = 1)

    val rdd2: RDD[Int] = sc.parallelize(
      seq = 7 to 100,
      numSlices = 2)

    // 数据处理
    // 1、subtract 用来取差集
    // 2、subtract 不会去重
    // 3、subtract 结果分区数取被减分区的分区数
    val subtractRdd: RDD[Int] = rdd.subtract(rdd2)

    // 数据输出
    subtractRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
