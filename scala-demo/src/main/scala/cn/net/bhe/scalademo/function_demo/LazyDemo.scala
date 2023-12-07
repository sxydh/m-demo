package cn.net.bhe.scalademo.function_demo

/*
 * 如果变量有lazy修饰，而且该变量是函数的返回值，则函数的执行被推迟，直到首次对该变量取值。
 */
object LazyDemo {

  def main(args: Array[String]): Unit = {
    def sum(a: Int, b: Int) = {
      println("实际执行sum")
      a + b
    }

    lazy val res = sum(1, 2)
    println("已经调用sum")
    println(s"res = ${res}")
  }

}
