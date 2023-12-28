package cn.net.bhe.sparkcoredemo.transdemo.pairrdddemo

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode,SpellCheckingInspection
object CoGroupDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    // 数据输入
    val rdd1 = sc.parallelize(List(("a", 1), ("b", 2), ("c", 3)))
    val rdd2 = sc.parallelize(List(("a", "apple"), ("b", "banana"), ("d", "dog")))

    // 数据处理
    // 1、cogroup 将两个 RDD 中具有相同键的元组放在一起  RDD[(K, (Iterable[V], Iterable[W1], Iterable[W2], Iterable[W3]))]
    // 2、cogroup 不是 RDD 的成员函数，而是 PairRDDFunctions 的成员函数，这里可以调用是因为有隐式转换函数 rddToPairRDDFunctions。
    // 3、cogroup 以元组的第一个值作为键
    val cogroupRdd: RDD[(String, (Iterable[Int], Iterable[String]))] = rdd1.cogroup(rdd2)

    // 数据输出
    cogroupRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
