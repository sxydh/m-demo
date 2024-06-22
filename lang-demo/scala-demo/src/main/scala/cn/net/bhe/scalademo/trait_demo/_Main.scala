package cn.net.bhe.scalademo.trait_demo

///////////// 特质定义 /////////////
// 1、特质类似于 Java 的接口（interfaces），但比接口更加灵活和功能丰富。
// 2、特质允许你定义一些方法和字段，然后将它们混入（mix in）到类中，从而给类增加额外的功能。
// 3、特质中的方法可以包含具体的实现，也可以是抽象的（没有实现）。
//noinspection ScalaUnusedSymbol
trait TraitDemo {

  ///////////// 抽象属性 /////////////
  val valInt: Int

  ///////////// 抽象方法 /////////////
  def fun(): Unit

  ///////////// 具体方法 /////////////
  def fun2(): Unit = {}
}

trait TraitDemo2 {}

///////////// 特质继承 /////////////
//noinspection ScalaUnusedSymbol
trait SubTraitDemo extends TraitDemo

//noinspection ScalaUnusedSymbol
trait SubTraitDemo2 extends TraitDemo with TraitDemo2

///////////// 特质实现 /////////////
//noinspection ScalaUnusedSymbol
class ClassDemo() extends TraitDemo {
  override val valInt: Int = 1

  override def fun(): Unit = {}
}

