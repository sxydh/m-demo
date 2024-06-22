package cn.net.bhe.scalademo.generic_demo

///////////// 类的泛型参数 /////////////
class ClassDemo[T] {

  var varT: T = _

  ///////////// 方法的泛型参数 /////////////
  def fun[X](x: X): X = {
    x
  }

}

//noinspection ScalaUnusedSymbol
object _Main {

  def main(args: Array[String]): Unit = {
    val strDemo = new ClassDemo[String]
    strDemo.varT = "Hello Word!"
    val varInt: Int = strDemo.fun(1)
  }

}
