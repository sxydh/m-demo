package cn.net.bhe.scalademo.class_object_demo

object SingletonDemo {
  def main(args: Array[String]): Unit = {
    val a = Singleton.getInstance()
    val b = Singleton.getInstance()
    println(a == b)
  }
}

class Singleton private {
}

object Singleton {
  private val singleton = new Singleton

  def getInstance(): Singleton = singleton
}