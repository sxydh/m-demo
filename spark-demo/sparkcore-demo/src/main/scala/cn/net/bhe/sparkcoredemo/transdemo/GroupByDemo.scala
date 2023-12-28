package cn.net.bhe.sparkcoredemo.transdemo

import cn.net.bhe.sparkcoredemo.wdInput
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object GroupByDemo {

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
    // 1、groupBy 之后一般情况下分区数不会变
    // 2、groupBy 之后各个数据项所在分区可能会变（即 Shuffle）
    // 3、groupBy 性能较差
    val groupByRdd: RDD[(Char, Iterable[String])] = rdd.groupBy(e => e.charAt(0))

    // 数据输出
    groupByRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
