package cn.net.bhe.scalademo.var_demo

//noinspection ScalaUnusedSymbol
object _Main {

  def main(args: Array[String]): Unit = {

    ///////////// 变量 /////////////
    var varInt: Int = 1 // 变量声明时必须初始化
    var varInt2 = 2 // 类型能够推导出来则可以省略
    var varNull: Null = null // 可以初始化变量为 Null
    var varString = "Hello World!"
    // varInt = varString // 错误操作，Scala 是强类型语言。

    ///////////// 常量 /////////////
    val valInt = 1
    val valString = "Hello World!"
  }

}
