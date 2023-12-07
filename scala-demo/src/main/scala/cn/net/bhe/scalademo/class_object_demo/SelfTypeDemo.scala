package cn.net.bhe.scalademo.class_object_demo

/* 自身类型可以实现依赖注入 */
object SelfTypeDemo {
  def main(args: Array[String]): Unit = {
    println(new Class13().get12())
  }
}

trait Trait11 {
  def get11(): Int = 11
}

trait Trait12 {
  /* 依赖注入Trait11 */
  this: Trait11 =>

  def get12(): Int = this.get11()
}


class Class13 extends Trait11 with Trait12 {}
