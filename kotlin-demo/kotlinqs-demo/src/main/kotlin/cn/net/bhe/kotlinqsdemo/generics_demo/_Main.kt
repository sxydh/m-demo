package cn.net.bhe.kotlinqsdemo.generics_demo

/************ 泛型定义 ************/
class ClassDemo<T>(val valT: T)

/************ 泛型协变 ************/
// out 协变
// 只能作为生产者
interface InterfaceDemo<out T> {
    fun f(): T
}

// in 协变
// 只能作为消费者
interface InterfaceDemo2<in T> {
    fun f(arg: T)
}

fun main() {
    println(">>>>>>>>>>>>> 泛型访问")
    val intClassDemo = ClassDemo<Int>(1)
    println("ClassDemo<Int>.varT = ${intClassDemo.valT}")
    val strClassDemo = ClassDemo<String>("Hello World!")
    println("ClassDemo<String>.varT = ${strClassDemo.valT}")
    println()

    println(">>>>>>>>>>>>> 泛型协变 out")
    fun outDemo(arg: InterfaceDemo<Int>) {
        // out 协变下，Number 作为 Int 的产出是安全的。
        val interfaceDemo: InterfaceDemo<Number> = arg
    }
    println()

    println(">>>>>>>>>>>>> 泛型协变 in")
    fun inDemo(arg: InterfaceDemo2<Number>) {
        // in 协变下，Int 当作 Number 消费是安全的。
        arg.f(1)
    }
    println()
}