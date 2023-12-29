package cn.net.bhe.sparkstreamingdemo.transdemo

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

//noinspection DuplicatedCode
object TransformDemo {

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
    // transform 可以获取底层的 RDD 并进行操作
    val transformDs: DStream[(Int, String)] = ds.transform(rdd => rdd.map(e => (e.length, e)))

    /* 数据输出 */
    transformDs.print()

    /* 释放资源 */
    ssc.start()
    ssc.awaitTermination()
  }

}
