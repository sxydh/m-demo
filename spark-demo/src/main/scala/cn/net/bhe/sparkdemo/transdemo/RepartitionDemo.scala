package cn.net.bhe.sparkdemo.transdemo

import cn.net.bhe.sparkdemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object RepartitionDemo {

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
    // repartition 本质是调用 coalesce(numPartitions, shuffle = true)
    val repartitionRdd: RDD[Int] = rdd.repartition(numPartitions = 1)

    // 数据输出
    repartitionRdd.foreach(println)

    // 关闭连接
    sc.stop()
  }

}
