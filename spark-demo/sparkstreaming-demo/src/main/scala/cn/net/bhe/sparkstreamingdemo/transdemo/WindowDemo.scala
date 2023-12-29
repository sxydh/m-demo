package cn.net.bhe.sparkstreamingdemo.transdemo

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

//noinspection DuplicatedCode
object WindowDemo {

  def main(args: Array[String]): Unit = {

    /* 环境准备 */
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(sparkConf, Seconds(3)) // 每个批次数据间隔时间

    /* 数据输入 */
    // 需要在 192.168.233.129 启动 Socket 服务：nc -lk 10010。
    val ds: DStream[String] = ssc.socketTextStream("192.168.233.129", 10010)

    /* 数据处理 */
    val windowDuration = Seconds(9) // 窗口大小
    val slideDuration = Seconds(3) // 滑动步长
    val windowDs: DStream[String] = ds.window(windowDuration, slideDuration)

    /* 数据输出 */
    windowDs.print()

    /* 释放资源 */
    ssc.start()
    ssc.awaitTermination()
  }

}
