package cn.net.bhe.sparkcoredemo.transdemo

import cn.net.bhe.sparkcoredemo.wdInput
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

//noinspection DuplicatedCode
object SampleDemo {

  def main(args: Array[String]): Unit = {

    // 环境准备
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    // 数据输入
    val rdd: RDD[String] = sc.textFile(
      path = wdInput,
      minPartitions = 4)

    // 数据处理
    val sampleRdd: RDD[String] = rdd.sample(
      // 样本被抽取出来后是否还能被再次抽取
      withReplacement = false,
      // 样本占比
      fraction = 0.001,
      // 随机种子
      seed = new Random().nextLong()
    )

    // 数据输出
    sampleRdd.foreach(println)

    // 释放资源
    sc.stop()
  }

}
