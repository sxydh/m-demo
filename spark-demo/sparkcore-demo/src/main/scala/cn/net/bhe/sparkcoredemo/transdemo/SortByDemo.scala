package cn.net.bhe.sparkcoredemo.transdemo

import cn.net.bhe.sparkcoredemo.wdInput
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object SortByDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    // 数据输入
    val rdd: RDD[String] = sc.textFile(
      path = wdInput,
      minPartitions = 4)

    // 数据处理
    val sortByRdd: RDD[String] = rdd.sortBy(e => e.length, ascending = true, numPartitions = 1)

    // 数据输出
    sortByRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
