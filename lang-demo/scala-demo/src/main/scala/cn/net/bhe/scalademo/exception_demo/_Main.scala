package cn.net.bhe.scalademo.exception_demo

//noinspection ScalaUnusedExpression
object _Main {

  def main(args: Array[String]): Unit = {

    ///////////// 异常处理 /////////////
    try {
      val a = 1
      val b = 0
      a / b
    } catch {
      case e: NumberFormatException =>
        e.printStackTrace()
      case e: Exception =>
        e.printStackTrace()
    } finally {
    }
  }

}
