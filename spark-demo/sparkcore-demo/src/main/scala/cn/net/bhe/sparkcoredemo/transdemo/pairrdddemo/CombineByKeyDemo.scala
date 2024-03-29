package cn.net.bhe.sparkcoredemo.transdemo.pairrdddemo

import cn.net.bhe.sparkcoredemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object CombineByKeyDemo {

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
    // 1、combineByKey 用于聚合
    // 2、combineByKey 不是 RDD 的成员函数，而是 PairRDDFunctions 的成员函数，这里可以调用是因为有隐式转换函数 rddToPairRDDFunctions。
    // 3、combineByKey 以元组的第一个值作为聚合键
    val createCombiner = (value: Int) => (value, 1) // 初始化函数（每个 key 的第一个值执行）
    val mergeValue = (combiner: (Int, Int), value: Int) => (combiner._1 + value, combiner._2 + 1) // 分区内聚合函数
    val mergeCombiners = (combiner1: (Int, Int), combiner2: (Int, Int)) => (combiner1._1 + combiner2._1, combiner1._2 + combiner2._2) // 分区键聚合函数
    val combineByKeyRdd: RDD[(Int, (Int, Int))] = rdd.map(e => (e % 2, e)).combineByKey(createCombiner, mergeValue, mergeCombiners)

    // 数据输出
    combineByKeyRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
