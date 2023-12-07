package cn.net.bhe.scalademo.operator_demo

object plus_demo {
  def main(args: Array[String]): Unit = {
    val a = 1
    val b = 1
    val c = a + b
    val d = a.+(b)
    println(c)
    println(d)
  }
}
