package cn.net.bhe.scalademo.class_object_demo

/* 枚举对象 */
object EnumDemo extends Enumeration {
  val ONE: Value = Value(1, "1")
  val TWO: Value = Value(2, "2")

  def main(args: Array[String]): Unit = {
    println(EnumDemo.ONE)
    println(EnumDemo.TWO)
  }
}
