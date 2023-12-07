package cn.net.bhe.scalademo.function_demo

import cn.net.bhe.mutil.{NumUtils, StrUtils}

/**
 * 匿名函数用Lamda表达式表示
 */
object AnonDemo {

  def main(args: Array[String]): Unit = {
    /* 基础用法 */
    var myPrintln = (any: Any) => System.out.println(any)
    myPrintln.apply(StrUtils.randomChs(6))

    /* 如果参数只用到一次，则可以用_代替，并省略参数列表。 */
    def agg(sum: (Int, Int) => Int, a: Int, b: Int) = {
      sum.apply(a, b)
    }

    println(agg(_ + _, NumUtils.ranInt(), NumUtils.ranInt()))
  }
}
