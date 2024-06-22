package cn.net.bhe.scalademo.extends_demo

class ClassDemo {

  var varInt = 1
  val valString = "Hello World!"

  def fun(): Unit = {}

}

///////////// 继承定义 /////////////
// 只能继承单个父类，但是可以实现多个特质（关键字 with）。
class SubClassDemo extends ClassDemo {

  ///////////// 重写父类属性 /////////////
  // 只有 val 可以重写
  override val valString: String = "Hello World!Hello World!"

  ///////////// 重写父类方法 /////////////
  override def fun(): Unit = {}

}

object _Main {

  def main(args: Array[String]): Unit = {
    new SubClassDemo().fun()
  }

}
