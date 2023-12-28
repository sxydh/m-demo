package cn.net.bhe.sparkdemo.transdemo

import cn.net.bhe.sparkdemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object InterSectDemo {

  def main(args: Array[String]): Unit = {

    // 建立连接
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    // 数据输入
    val rdd: RDD[Int] = sc.parallelize(
      seq = intSeq,
      numSlices = 1)

    val rdd2: RDD[Int] = sc.parallelize(
      seq = 5 to 7,
      numSlices = 2)

    // 数据处理
    // 1、intersection 用来取交集
    // 2、intersection 会去重
    // 3、intersection 结果分区数取较大者
    val intersectionRdd: RDD[Int] = rdd.intersection(rdd2)

    // 数据输出
    intersectionRdd.foreach(println)

    // 关闭连接
    sc.stop()
  }

}
