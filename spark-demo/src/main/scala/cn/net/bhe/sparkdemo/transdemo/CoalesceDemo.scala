package cn.net.bhe.sparkdemo.transdemo

import cn.net.bhe.sparkdemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object CoalesceDemo {

  def main(args: Array[String]): Unit = {

    // 建立连接
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    // 数据输入
    val rdd: RDD[Int] = sc.parallelize(
      seq = intSeq,
      numSlices = 4)

    // 数据处理
    // 1、coalesce 用来缩减分区
    // 2、coalesce 不会引起数据 Shuffle
    val coalesceRdd: RDD[Int] = rdd.coalesce(numPartitions = 1)

    // 数据输出
    coalesceRdd.foreach(println)

    // 关闭连接
    sc.stop()
  }

}
