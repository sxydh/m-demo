package cn.net.bhe.sparkstreamingdemo.inputdemo

import cn.net.bhe.mutil.StrUtils
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

//noinspection DuplicatedCode
object ReceiverDemo {

  def main(args: Array[String]): Unit = {

    /* 环境准备 */
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(sparkConf, Seconds(3)) // 每个批次数据间隔时间

    /* 数据输入 */
    val ds: DStream[String] = ssc.receiverStream(new CustomReceiver)

    /* 数据输出 */
    ds.print()

    /* 释放资源 */
    ssc.start()
    ssc.awaitTermination()
  }

}

class CustomReceiver extends Receiver[String](StorageLevel.MEMORY_ONLY) {

  private var flag = true

  override def onStart(): Unit = {
    new Thread(() => {
      while (flag) {
        store(StrUtils.randomChs(10))
        Thread.sleep(500)
      }
    }).start()
  }

  override def onStop(): Unit = {
    flag = false
  }

}
