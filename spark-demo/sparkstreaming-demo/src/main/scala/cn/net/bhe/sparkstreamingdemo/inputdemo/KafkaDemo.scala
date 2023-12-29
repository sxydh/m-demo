package cn.net.bhe.sparkstreamingdemo.inputdemo

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

//noinspection DuplicatedCode
object KafkaDemo {

  def main(args: Array[String]): Unit = {

    /* 环境准备 */
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(sparkConf, Seconds(3)) // 每个批次数据间隔时间

    /* 数据输入 */
    val kafkaConf = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop01:9092,hadoop02:9092,hadoop03:9092",
      ConsumerConfig.GROUP_ID_CONFIG -> "SSK_DEMO",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer"
    )
    val ds: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](Set("word"), kafkaConf))

    /* 数据处理 */
    val mapDs = ds.map(_.value())

    /* 数据输出 */
    mapDs.print()

    /* 释放资源 */
    ssc.start()
    ssc.awaitTermination()
  }

}
