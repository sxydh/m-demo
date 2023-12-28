package cn.net.bhe.sparkdemo.transdemo

import cn.net.bhe.sparkdemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object AggregateDemo {

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
    val zeroValue: Int = 0 // 聚合初始值
    val seqOp = (acc: Int, value: Int) => acc + value // 分区内聚合函数
    val combOp = (acc: Int, value: Int) => acc + value // 分区间聚合函数
    val aggregate: Int = rdd.aggregate(zeroValue)(seqOp, combOp)
    println(aggregate)

    // 关闭连接
    sc.stop()
  }

}
