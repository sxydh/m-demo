package cn.net.bhe.scalademo.enum_demo

object EnumDemo extends Enumeration {
  val ONE: Value = Value(1, "1")
  val TWO: Value = Value(2, "2")

  def main(args: Array[String]): Unit = {
    println(EnumDemo.ONE)
    println(EnumDemo.TWO)
  }
}

object _Main extends Enumeration {
  def main(args: Array[String]): Unit = {
    println(EnumDemo.ONE)
    println(EnumDemo.TWO)
  }
}
