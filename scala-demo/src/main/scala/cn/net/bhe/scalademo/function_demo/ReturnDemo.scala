package cn.net.bhe.scalademo.function_demo

import cn.net.bhe.mutil.NumUtils

object ReturnDemo {

  /* 没有返回类型 */
  def f(): Unit = {}

  /* 没有返回类型简化 */
  def f2() {}

  /* 显示声明返回类型 */
  def f3(): Int = NumUtils.ONE

  /* 返回类型省略（前提是类型可以推导） */
  def f4() = NumUtils.ONE

  /* 返回类型是函数 */
  def f5(): Any => Unit = {
    def myPrintln(any: Any) = {
      System.out.println(any)
    }

    myPrintln
  }

  def main(args: Array[String]): Unit = {
    println(f())
    println(f2())
    println(f3())
    println(f4())
    println(f5())
  }

}
