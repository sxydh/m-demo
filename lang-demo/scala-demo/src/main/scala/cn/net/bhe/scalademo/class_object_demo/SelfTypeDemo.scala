package cn.net.bhe.scalademo.class_object_demo

///////////// 自身类型 /////////////
// 1、自身类型是一种用于声明一个类或特质必须混入另一个类或特质的机制
// 2、通过自身类型，你可以要求一个类或特质必须包含指定类型的成员，或者必须扩展自某个特定的类。

trait TraitDemo {
  val valInt = 1
}

trait TraitDemo2 {
  self: TraitDemo => // 混入 TraitDemo，类似 extends。
  def intFun(): Int = {
    valInt // 可以使用 TraitDemo 成员
  }
}

//noinspection ScalaUnusedSymbol
class TraitImplDemo extends TraitDemo2 with TraitDemo { // 实现 TraitDemo2 时必须同时实现 TraitDemo
}