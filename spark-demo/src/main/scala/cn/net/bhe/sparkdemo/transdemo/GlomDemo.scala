package cn.net.bhe.sparkdemo.transdemo

import cn.net.bhe.sparkdemo.wdInput
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//noinspection DuplicatedCode,SpellCheckingInspection
object GlomDemo {

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
    // glom 把每个分区的数据转为一个数组
    val glomRdd: RDD[Array[String]] = rdd.glom()

    // 数据输出
    glomRdd.foreach(e => println(e.mkString(" ")))

    // 关闭连接
    sc.stop()
  }

}
