package cn.net.bhe.scalademo.class_object_demo

import scala.io.StdIn

object ExceptionDemo extends App {
  while (true) {
    val strA = StdIn.readLine()
    val strB = StdIn.readLine()
    try {
      val a = strA.toInt
      val b = strB.toInt
      val c = a / b
      println(s"计算结果 = ${c}")
    } catch {
      case e: NumberFormatException => {
        println(s"检析异常 = ${e.getLocalizedMessage}")
      }
      case e: Exception => {
        println(s"其它异常 = ${e.getLocalizedMessage}")
      }
    } finally {
      println(s"输入 = ${strA}, ${strB}")
    }
  }
}
