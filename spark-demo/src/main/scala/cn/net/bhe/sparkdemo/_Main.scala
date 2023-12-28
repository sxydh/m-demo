package cn.net.bhe.sparkdemo

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object _Main {

  def main(args: Array[String]): Unit = {

    //  建立连接
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("WordCount")
    val sc = new SparkContext(sparkConf)

    //  数据输入
    val lines: RDD[String] = sc.textFile(wdInput)

    //  数据处理
    val words: RDD[String] = lines.flatMap(_.split(" "))
    val wordGroup: RDD[(String, Iterable[String])] = words.groupBy(word => word)
    val wordCount: RDD[(String, Int)] = wordGroup.map(item => (item._1, item._2.size))

    //  数据输出
    val res: Array[(String, Int)] = wordCount.collect()
    println(res.mkString(","))

    //  关闭连接
    sc.stop()
  }

}
