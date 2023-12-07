package cn.net.bhe.scalademo.class_object_demo

import scala.language.implicitConversions

object ImplicitDemo extends App {

  /* 隐式函数anyName扩展了String的方法 */
  implicit def anyName(str: String): MyString = new MyString(str)

  println("1".isNum)

  /* 隐式类ImplicitString扩展了String的方法 */
  implicit class ImplicitString(val str: String) {
    def isNotNum: Boolean = !str.forall(Character.isDigit)
  }

  println("1".isNotNum)

  /* 隐式参数one优先级比默认参数高 */
  implicit val one = 1

  def printi(implicit i: Int = 9) = println(i)

  printi
}

class MyString(val str: String) {
  def isNum: Boolean = str.forall(Character.isDigit)
}
