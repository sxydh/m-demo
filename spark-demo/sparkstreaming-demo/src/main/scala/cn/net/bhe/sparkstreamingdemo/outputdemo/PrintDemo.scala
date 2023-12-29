package cn.net.bhe.sparkstreamingdemo.outputdemo

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

//noinspection DuplicatedCode
object PrintDemo {

  def main(args: Array[String]): Unit = {

    /* 环境准备 */
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(sparkConf, Seconds(3)) // 每个批次数据间隔时间

    /* 数据输入 */
    // 需要在 192.168.233.129 启动 Socket 服务：nc -lk 10010。
    val ds: DStream[String] = ssc.socketTextStream("192.168.233.129", 10010)

    /* 数据输出 */
    ds.print()

    /* 释放资源 */
    ssc.start()
    ssc.awaitTermination()
  }

}
