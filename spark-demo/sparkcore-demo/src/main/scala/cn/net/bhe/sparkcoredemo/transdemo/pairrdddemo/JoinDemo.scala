package cn.net.bhe.sparkcoredemo.transdemo.pairrdddemo

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object JoinDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    // 数据输入
    val rdd1 = sc.parallelize(List(("a", 1), ("b", 2), ("c", 3)))
    val rdd2 = sc.parallelize(List(("a", "apple"), ("b", "banana"), ("d", "dog")))

    // 数据处理
    // 1、join 类似于 SQL 的 join
    // 2、join 不是 RDD 的成员函数，而是 PairRDDFunctions 的成员函数，这里可以调用是因为有隐式转换函数 rddToPairRDDFunctions。
    // 3、join 以元组的第一个值作为键
    val joinRdd: RDD[(String, (Int, String))] = rdd1.join(rdd2)

    // 数据输出
    joinRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
