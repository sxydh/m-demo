package cn.net.bhe.sparkcoredemo.transdemo.pairrdddemo

import cn.net.bhe.sparkcoredemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object FoldByKeyDemo {

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

    // 数据处理
    // 1、foldByKey 用于聚合
    // 2、foldByKey 不是 RDD 的成员函数，而是 PairRDDFunctions 的成员函数，这里可以调用是因为有隐式转换函数 rddToPairRDDFunctions。
    // 3、foldByKey 以元组的第一个值作为聚合键
    val zeroValue = 0 // 聚合初始值
    val foldOp = (acc: Int, value: Int) => acc + value // 聚合函数
    val foldByKeyRdd: RDD[(Int, Int)] = rdd.map(e => (e % 2, e)).foldByKey(zeroValue)(foldOp)

    // 数据输出
    foldByKeyRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
