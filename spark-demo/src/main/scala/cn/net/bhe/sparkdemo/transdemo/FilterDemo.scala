package cn.net.bhe.sparkdemo.transdemo

import cn.net.bhe.sparkdemo.wdInput
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object FilterDemo {

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

    // 数据处理
    val filterRdd: RDD[String] = rdd.filter(e => e.length < 5)

    // 数据输出
    filterRdd.foreach(println)

    // 关闭连接
    sc.stop()
  }

}
