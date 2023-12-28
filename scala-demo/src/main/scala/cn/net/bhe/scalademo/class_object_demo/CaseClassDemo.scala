package cn.net.bhe.scalademo.class_object_demo

///////////// 样例类 /////////////
// 样例类（Case Class）是 Scala 中一种特殊的类，它具有许多自动生成的特性。
// 1、构造函数的参数被当作字段来使用
// 2、自动生成 toString 方法
// 3、自动生成 equals 和 hashCode 方法
// 4、模式匹配支持
// 5、自动生成 copy 方法
// 6、可序列化
case class CaseClassDemo(int: Int, string: String)


object CaseClassDemo {

  def main(args: Array[String]): Unit = {
    val caseClassDemo = new CaseClassDemo(1, "1")
    val caseClassDemo2 = caseClassDemo.copy()
    println(caseClassDemo == caseClassDemo2)
  }

}
