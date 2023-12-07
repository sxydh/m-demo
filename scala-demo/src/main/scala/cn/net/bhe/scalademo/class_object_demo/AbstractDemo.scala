package cn.net.bhe.scalademo.class_object_demo

import cn.net.bhe.mutil.StrUtils

object AbstractDemo {
  def main(args: Array[String]): Unit = {
    new Man().talk()
  }
}

/* 抽象类 */
abstract class Human {
  /* 抽象方法 */
  def talk(): Unit
}

/* 抽象类实现 */
class Man extends Human {
  /* 抽象方法实现 */
  def talk(): Unit = {
    println(StrUtils.HELLO_WORLD)
  }
}