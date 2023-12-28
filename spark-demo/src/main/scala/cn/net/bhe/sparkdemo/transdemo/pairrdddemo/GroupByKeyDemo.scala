package cn.net.bhe.sparkdemo.transdemo.pairrdddemo

import cn.net.bhe.sparkdemo.intSeq
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode
object GroupByKeyDemo {

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
    // 1、groupByKey 用于分组
    // 2、groupByKey 性能较差
    // 3、groupByKey 不是 RDD 的成员函数，而是 PairRDDFunctions 的成员函数，这里可以调用是因为有隐式转换函数 rddToPairRDDFunctions。
    // 4、groupByKey 以元组的第一个值作为分组键
    val groupByKeyRdd: RDD[(Int, Iterable[Int])] = rdd.map(e => (e % 2, e)).groupByKey()

    // 数据输出
    groupByKeyRdd.foreach(println)

    // 关闭连接
    sc.stop()
  }

}
