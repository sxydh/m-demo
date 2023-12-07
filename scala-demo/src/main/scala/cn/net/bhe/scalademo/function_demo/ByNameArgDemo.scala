package cn.net.bhe.scalademo.function_demo

import scala.util.Random

/*
 * 传名参数仅在被使用时才会触发实参的求值运算，与传值参数正好相反。
 * 要将一个参数变为传名参数，只需在它的类型前加上=>。
 * 如果参数是计算密集型或长时间运行的代码块，如获取URL，使用传名参数可以提高性能。
 */
object ByNameArgDemo {

  def getLen(input: => Int) = {
    println("已经取值")
    String.valueOf(input).length
  }

  def main(args: Array[String]): Unit = {
    def getInput: Int = {
      println("实际取值")
      new Random().nextInt()
    }

    /*
     * 输出是：
     * 已经调用
     * 实际调用
     * 11
     */
    println(getLen(getInput))
  }

}
