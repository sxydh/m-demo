package cn.net.bhe.sparkstreamingdemo.inputdemo

import cn.net.bhe.sparkstreamingdemo.queue
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

//noinspection DuplicatedCode
object QueueDemo {

  def main(args: Array[String]): Unit = {

    /* 环境准备 */
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(sparkConf, Seconds(3)) // 每个批次数据间隔时间

    /* 数据输入 */
    val ds: DStream[String] = ssc.queueStream(queue, oneAtATime = false)

    /* 数据输出 */
    ds.print()

    /* 释放资源 */
    ssc.start()
    for (it <- 1 to 100) {
      queue.enqueue(ssc.sparkContext.parallelize(Seq(it.toString)))
      Thread.sleep(1000)
    }
    ssc.awaitTermination()
  }

}
