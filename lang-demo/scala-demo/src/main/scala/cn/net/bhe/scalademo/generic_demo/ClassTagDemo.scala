package cn.net.bhe.scalademo.generic_demo

import scala.reflect.ClassTag

///////////// ClassTag /////////////
// 1、ClassTag 是一种运行时类型标签，用于在运行时获取泛型类型的信息。
// 2、ClassTag 是在泛型类或方法中使用的一种机制，以克服类型擦除（type erasure）引入的信息丢失问题。
class ClassTagDemo[T, X](implicit tt: ClassTag[T], xt: ClassTag[X]) {

  def tType(): Class[_] = {
    tt.runtimeClass
  }

  def xType(): Class[_] = {
    xt.runtimeClass
  }

}


object ClassTagDemo {

  def main(args: Array[String]): Unit = {
    val classTagDemo = new ClassTagDemo[Int, String]
    println(classTagDemo.tType())
    println(classTagDemo.xType())
  }

}
