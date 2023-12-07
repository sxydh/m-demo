package cn.net.bhe.scalademo.function_demo

import cn.net.bhe.mutil.{NumUtils, StrUtils}

object ArgDemo {

  /* 空参 */
  def f() = println()

  /* 一个参数 */
  def f2(i: Int) = println(i)

  /* 多个参数 */
  def f3(i: Int, str: String) = println(s"${i}, ${str}")

  /* 参数带默认值 */
  def f4(i: Int = NumUtils.ONE, str: String = StrUtils.HELLO_WORLD) = println(s"${i}, ${str}")

  /* 可变参数 */
  def f5(strs: String*): Unit = println(strs)

  /* 参数是函数 */
  def f6(any: String => Unit, str: String) = any.apply(str)

  def main(args: Array[String]): Unit = {
    f()
    f2(NumUtils.ranInt())
    f3(NumUtils.ranInt(), StrUtils.randomChs(6))
    f4()
    f5(StrUtils.randomChs(6), StrUtils.randomChs(6))
    f6(println, StrUtils.randomChs(6))
  }
}
