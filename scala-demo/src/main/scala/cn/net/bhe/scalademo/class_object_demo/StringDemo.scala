package cn.net.bhe.scalademo.class_object_demo

object StringDemo {
  def main(args: Array[String]): Unit = {
    val str = "Hello World!"
    println(str + str)

    println(str * 2)

    println(s"${str}")
  }
}
