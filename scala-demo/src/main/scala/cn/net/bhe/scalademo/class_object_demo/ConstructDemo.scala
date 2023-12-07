package cn.net.bhe.scalademo.class_object_demo

import cn.net.bhe.mutil.StrUtils

import scala.util.Random

/* 主构造函数 */
class ConstructDemo(var i: Int, val str: String) {

  var o: Object = new Object

  /* 辅构造函数 */
  def this() {
    this(new Random().nextInt(), StrUtils.randomChs(6))
  }
}

object ConstructDemo {
  def main(args: Array[String]): Unit = {
    val demo = new ConstructDemo()
    println(s"${demo.i}, ${demo.str}, ${demo.o}")
  }
}
