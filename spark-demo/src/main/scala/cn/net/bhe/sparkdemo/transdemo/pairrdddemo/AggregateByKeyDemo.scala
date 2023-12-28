package cn.net.bhe.sparkdemo.transdemo.pairrdddemo

import cn.net.bhe.sparkdemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object AggregateByKeyDemo {

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

    // 数据处理
    // 1、aggregateByKey 用于聚合
    // 2、aggregateByKey 不是 RDD 的成员函数，而是 PairRDDFunctions 的成员函数，这里可以调用是因为有隐式转换函数 rddToPairRDDFunctions。
    // 3、aggregateByKey 以元组的第一个值作为聚合键
    val zeroValue: Int = 0 // 聚合初始值
    val seqOp = (acc: Int, value: Int) => acc + value // 分区内聚合函数
    val combOp = (acc: Int, value: Int) => acc + value // 分区间聚合函数
    val aggregateByKeyRdd: RDD[(Int, Int)] = rdd.map(e => (e % 2, e)).aggregateByKey(zeroValue)(seqOp, combOp)

    // 数据输出
    aggregateByKeyRdd.foreach(println)

    // 关闭连接
    sc.stop()
  }

}
