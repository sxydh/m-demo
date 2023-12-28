package cn.net.bhe.sparkcoredemo.transdemo.pairrdddemo

import cn.net.bhe.sparkcoredemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}

//noinspection DuplicatedCode
object PartitionByDemo {

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

    // 数据处理
    // 1、partitionBy 用于重分区
    // 2、partitionBy 不是 RDD 的成员函数，而是 PairRDDFunctions 的成员函数，这里可以调用是因为有隐式转换函数 rddToPairRDDFunctions。
    // 3、HashPartitioner 以元组的第一个值作为分区键
    val partitionByRdd: RDD[(Int, Int)] = rdd.map(e => (1, e)).partitionBy(new HashPartitioner(4))

    // 数据输出
    partitionByRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
