package cn.net.bhe.sparkdemo.inputdemo

import cn.net.bhe.sparkdemo.wdInput
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object TextFileDemo {

  def main(args: Array[String]): Unit = {

    // 建立连接
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    // 数据输入
    val rdd: RDD[String] = sc.textFile(
      path = wdInput,
      minPartitions = 4)

    // 数据输出
    rdd.foreach(println)

    // 关闭连接
    sc.stop()
  }

}
