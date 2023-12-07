package cn.net.bhe.scalademo.class_object_demo


class VarDemo {
  /*
   * var用来声明变量
   * 变量声明时必须赋初值。如果没有初值，可以用“_”代替（_表示通配符），或者声明为抽象属性。
   * 如果变量类型可以推导，则可以省略。
   * Scala是强类型语言，两个类型不兼容的变量不能进行赋值。
   */
  var i: Int = 1

  /* val用来声明final的变量 */
  val I = 1
}

object VarDemo {
  var s = "1"
  val S = "1"

  def main(args: Array[String]): Unit = {
    var o = new Object()
    val O = new Object()
  }
}
