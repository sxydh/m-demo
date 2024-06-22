package cn.net.bhe.kotlinqsdemo.interface_demo

/************ 接口定义 ************/
interface InterfaceDemo {

    // 抽象属性
    var varInt: Int

    // 抽象函数
    fun f()

    // 普通函数
    fun f2() {
        println("InterfaceDemo.f2() 被调用")
    }
}

/************ 接口继承 ************/
interface InterfaceDemo2 : InterfaceDemo {
    var varString: String
}

/************ 实现单接口 ************/
class ClassDemo : InterfaceDemo {
    // 属性实现
    override var varInt: Int = 1

    // 函数实现
    override fun f() {
        println("ClassDemo.f() 被调用，ClassDemo.varInt=${this.varInt}")
    }
}

/************ 实现多接口 ************/
class ClassDemo2 : InterfaceDemo, InterfaceDemo2 {
    override var varInt: Int = 1
    override var varString: String = "Hello World!"
    override fun f() {
        println("ClassDemo2.f() 被调用，ClassDemo2.varInt=${this.varInt}，ClassDemo2.varString=${this.varString}")
    }
}

fun main() {
    /************ 接口访问 ************/
    println(">>>>>>>>>>>>> 实现单接口")
    val classDemo = ClassDemo()
    classDemo.f()
    classDemo.f2()
    println()

    println(">>>>>>>>>>>>> 实现多接口")
    val classDemo2 = ClassDemo2()
    classDemo2.f()
    classDemo2.f2()
    println()
}