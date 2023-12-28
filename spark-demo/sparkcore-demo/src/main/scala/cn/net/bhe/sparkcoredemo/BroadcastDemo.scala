package cn.net.bhe.sparkcoredemo

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object BroadcastDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)
    val bc: Broadcast[Seq[String]] = sc.broadcast(wdSeq) // 广播变量

    // 数据输入
    val rdd: RDD[Int] = sc.parallelize(
      seq = 1 to 10,
      numSlices = 10)

    rdd.foreach(e => println(bc.value(e)))

    // 释放资源
    sc.stop()
  }

}
