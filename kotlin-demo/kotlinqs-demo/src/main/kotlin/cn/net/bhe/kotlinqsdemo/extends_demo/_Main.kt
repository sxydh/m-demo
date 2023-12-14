@file:Suppress("ConvertSecondaryConstructorToPrimary")

package cn.net.bhe.kotlinqsdemo.extends_demo

/************ 继承概述 ************/
// 所有的类隐式继承自 Any
// 类和成员函数默认是 final 的，想要被继承必须显示 open 修饰。
open class ClassDemo(var varInt: Int) {
    open var varString: String = "Hello World!"
    open fun f() {
        println("ClassDemo.f() 被调用")
    }
}

/************ 继承定义 ************/
// 子类必须显示调用父类构造函数
// 主构造函数调用
class SubClassDemo : ClassDemo(1) {}

// 辅构造函数调用
class SubClassDemo2 : ClassDemo {
    constructor() : super(1) {}
}

/************ 重写父类成员函数 ************/
class SubClassDemo3 : ClassDemo(1) {
    override fun f() {
        println("SubClassDemo3.f() 被调用")
    }
}

/************ 重写父类成员属性 ************/
class SubClassDemo4 : ClassDemo(1) {
    override var varString: String = "Hello World!Hello World!"
    override fun f() {
        print("SubClassDemo4.f() 被调用：SubClassDemo4.varString = ${varString}，ClassDemo.varString = ${super.varString}")
    }
}

fun main() {
    /************ 继承访问 ************/
    println(">>>>>>>>>>>>> 重写成员函数")
    val subClassDemo3 = SubClassDemo3()
    subClassDemo3.f()
    println()

    println(">>>>>>>>>>>>> 重写成员属性")
    val subClassDemo4 = SubClassDemo4()
    subClassDemo4.f()
    println()
}