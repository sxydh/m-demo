package cn.net.bhe.scalademo.class_object_demo

///////////// 类定义 /////////////
//noinspection ScalaUnusedSymbol
class ClassDemo
///////////// 主构造函数 /////////////
// var 和 val 修饰的参数是类成员
(var varInt: Int) {

  ///////////// 成员属性 /////////////
  var varString = "Hello World!"
  var varAny: Any = _ // 如果成员属性值未确定，可以初始化为通配符 _ 。

  ///////////// 辅助构造函数 /////////////
  // 辅助构造函数需要显示调用主构造函数
  def this(argInt: Int, argString: String) {
    this(argInt)
    this.varString = argString
  }

  ///////////// 成员方法 /////////////
  def fun(): Unit = {
  }
}

///////////// 抽象类 /////////////
abstract class AbstractClassDemo {
  ///////////// 抽象属性 /////////////
  var varInt: Int

  ///////////// 抽象方法 /////////////
  def fun(): Unit
}

object _Main {

  def main(args: Array[String]): Unit = {
    new ClassDemo(1).fun()

    new AbstractClassDemo {
      override var varInt: Int = 1

      override def fun(): Unit = println()
    }.fun()
  }

}
