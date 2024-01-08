package cn.net.bhe.sparkcoredemo.sparklauncherdemo

import cn.net.bhe.mutil.DtUtils
import com.sun.net.httpserver.{HttpExchange, HttpServer}
import org.apache.spark.launcher.{SparkAppHandle, SparkLauncher}

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

object _Main {

  def main(args: Array[String]): Unit = {
    val server = HttpServer.create(new InetSocketAddress(30006), 0)
    server.createContext(
      "/",
      (exchange: HttpExchange) => {
        if (exchange.getRequestURI.getPath == "/") {
          val start = System.currentTimeMillis()
          new SparkLauncher()
            .setSparkHome("/opt/module/spark/spark-3.0.0-bin-hadoop3.2")
            .setAppResource("/opt/module/tmp/sparkcore-demo-1.0-SNAPSHOT.jar")
            .setMainClass("cn.net.bhe.sparkcoredemo.sparklauncherdemo.SimpleJob")
            .setMaster("spark://hadoop01:7077")
            .setConf(SparkLauncher.DRIVER_MEMORY, "2g")
            .setConf(SparkLauncher.EXECUTOR_MEMORY, "512m")
            .startApplication(new SparkAppHandle.Listener() {
              override def stateChanged(handle: SparkAppHandle): Unit = {
                println(s"stateChanged: isFinal = ${handle.getState.isFinal}, mills = ${System.currentTimeMillis() - start}")
              }

              override def infoChanged(handle: SparkAppHandle): Unit = {
                println(s"infoChanged: appId = ${handle.getAppId}, state = ${handle.getState}")
              }
            })
        }

        val body = DtUtils.ts17().getBytes(StandardCharsets.UTF_8)
        exchange.sendResponseHeaders(200, body.length)
        exchange.getResponseBody.write(body)
        exchange.close()
      })
    server.start()
  }

}
