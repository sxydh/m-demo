package cn.net.bhe.scalademo.type_demo

//noinspection ScalaUnusedSymbol,ScalaUnusedExpression,VarCouldBeVal
object _Main {

  ///////////// 数据类型 /////////////
  // 详见 https://docs.scala-lang.org/tour/unified-types.html
  def main(args: Array[String]): Unit = {

    ///////////// 数字 /////////////
    var varByte: Byte = 1
    var varShort: Short = 1
    var varInt: Int = 1
    var varLong: Long = 1L

    var varFloat: Float = 1.1F
    var varDouble: Double = 1.1

    ///////////// 布尔 /////////////
    var varBoolean: Boolean = false

    ///////////// 字符 /////////////
    var varChar: Char = 'a'

    ///////////// Option /////////////
    // Option 用于表示可能存在或可能不存在值的容器类型
    var varOption: Option[Int] = Some(1) // Option 有两个子类 Some 和 None
    var varOption2: Option[Int] = None // Option 可以避免给变量初始化为 Null
    varOption.getOrElse(1) // Option 获取值

    ///////////// 字符串 /////////////
    var varString: String = "Hello World!"
    var varString2: String = "Hello World!" * 2 // Scala 字符串的运算符被重载了
    var varString3: String = s"${varString * 2}" // 字符串占位符

    ///////////// 数组 /////////////
    var intArr: Array[Int] = Array(1, 2, 3)
    var strArr: Array[String] = Array("Hello", " ", "World", "!")
  }

}
