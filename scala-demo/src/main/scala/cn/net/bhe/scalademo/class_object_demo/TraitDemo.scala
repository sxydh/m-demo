package cn.net.bhe.scalademo.class_object_demo

object TraitDemo {
  def main(args: Array[String]): Unit = {
    println(new Class)
    println(new Class2)
    println(new Class3)
  }
}

/* 特质相当于Java的接口，用trait修饰符修饰。 */
trait Trait {}

trait Trait2 {}

class SuperClass {}

/* 继承单个Trait */
class Class extends Trait {}

/* 继承多个Trait */
class Class2 extends Trait with Trait2 {}

/* 继承父类和Trait */
class Class3 extends SuperClass with Trait with Trait2 {}