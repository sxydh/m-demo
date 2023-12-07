package cn.net.bhe.scalademo.function_demo

import cn.net.bhe.mutil.{DtUtils, StrUtils}

/*
 * 柯里化（Currying）是一种关于函数的高阶技术，它是指将一个函数从可调用的f(a, b, c)转换为可调用的f(a)(b)(c)。
 * 柯里化实现用到了闭包，所谓闭包就是：如果一个函数，访问到了它的外部（局部）变量的值，那么这个函数和它所处的环境，称为闭包。
 * 函数柯里化的一个好处是：可以复用函数过程。
 */
object CurryingDemo {

  /* log函数 */
  def log(date: String, level: String, msg: String): Unit = {
    println(s"${date}, ${level}, ${msg}")
  }

  /* 柯里化log */
  def log2(date: String): String => String => Unit = {
    def levelFunc(level: String): String => Unit = {
      def msgFunc(msg: String): Unit = {
        println(s"${date}, ${level}, ${msg}")
      }

      msgFunc
    }

    levelFunc
  }

  def main(args: Array[String]): Unit = {
    val dateLog = log2(DtUtils.format())
    val debug = dateLog("DEBUG")
    val info = dateLog("INFO")
    val warn = dateLog("WARN")

    debug(StrUtils.randomChs(6))
    info(StrUtils.randomChs(6))
    warn(StrUtils.randomChs(6))
  }

}
