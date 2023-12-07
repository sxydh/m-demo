package cn.net.bhe.scalademo.control_demo

import scala.language.postfixOps
import scala.util.control.Breaks.break

object ForDemo {
  def main(args: Array[String]): Unit = {
    /* 基础循环 */
    for (i <- 1 to 10) {
      print(i)
    }
    println()
    for (i <- 1 until 10) {
      print(i)
    }
    println()
    for (i <- Array(1, 2, 3, 4)) {
      print(i)
    }
    println()

    /* 循环守卫 */
    for (i <- 1 to 10 if i != 3) {
      print(i)
    }
    println()

    /* 循环步长 */
    for (i <- 1 to 10 by 2) {
      print(i)
    }
    for (i <- 1 to 10 reverse) {
      print(i)
    }
    println()

    /* 循环嵌套 */
    for (i <- 1 to 2; j <- 1 to 3) {
      print(s"${i}-${j},")
    }

    /* 引入变量 */
    println()
    for (i <- 1 to 2; j = i + 3) {
      print(s"${i}-${j},")
    }
    println()
    for {
      i <- 1 to 2
      j = i + 3
    } {
      print(s"${i}-${j},")
    }
    println()

    /* 循环返回值 */
    val is = for (i <- 1 to 10) yield i * 2
    print(is)
    println()

    /*
     * 循环中断
     * 注意：break不是关键字，而是Breaks的方法，break的本质是抛出breakException。
     */
    for (i <- 1 to 10) {
      if (i == 4) {
        break
      }
      print(i)
    }
    println()
  }
}
