package cn.net.bhe.sparkcoredemo.transdemo.pairrdddemo

import cn.net.bhe.sparkcoredemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object ReduceByKeyDemo {

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
    // 1、reduceByKey 用于聚合
    // 2、reduceByKey 不是 RDD 的成员函数，而是 PairRDDFunctions 的成员函数，这里可以调用是因为有隐式转换函数 rddToPairRDDFunctions。
    // 3、reduceByKey 以元组的第一个值作为聚合键
    val reduceByKeyRdd: RDD[(Int, Int)] = rdd.map(e => (e % 2, e)).reduceByKey(_ + _)

    // 数据输出
    reduceByKeyRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
