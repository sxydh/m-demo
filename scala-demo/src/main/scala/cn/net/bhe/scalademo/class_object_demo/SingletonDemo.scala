package cn.net.bhe.scalademo.class_object_demo

///////////// 单例实现 /////////////

// 1、隐藏单例类的构造函数
//noinspection ScalaUnusedSymbol
class SingletonDemo private() {
}

// 2、使用伴生对象获取单例类的实例
//noinspection ScalaUnusedSymbol
object SingletonDemo {
  private val singletonDemo = new SingletonDemo

  def getInstance(): SingletonDemo = singletonDemo
}