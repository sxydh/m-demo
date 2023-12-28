package cn.net.bhe.sparkcoredemo.transdemo

import cn.net.bhe.sparkcoredemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object UnionDemo {

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
      seq = 50 to 51,
      numSlices = 2)

    // 数据处理
    // 1、union 用来取并集
    // 2、union 不会去重
    // 3、union 结果分区数取两者之和
    val unionRdd: RDD[Int] = rdd.union(rdd2)

    // 数据输出
    unionRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
