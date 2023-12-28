package cn.net.bhe.sparkdemo

import org.apache.spark.rdd.RDD
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object LongAccumulatorDemo {

  def main(args: Array[String]): Unit = {

    // 建立连接
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)
    val acc: LongAccumulator = sc.longAccumulator("LongAccumulator") // 共享累加器

    // 数据输入
    val rdd: RDD[String] = sc.textFile(
      path = wdInput,
      minPartitions = 4)

    rdd.foreach(_ => acc.add(1L))
    println(acc.value)

    // 关闭连接
    sc.stop()
  }

}
