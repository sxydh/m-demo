package cn.net.bhe.kotlinqsdemo.delegate_demo

interface Interface {
    fun f()
}

class ClassDemo : Interface {
    override fun f() {
        println("ClassDemo.f() 被调用")
    }
}

/************ 委托定义 ************/
// 代理模式的实现
// by 只能修饰接口
class DelegateDemo(arg: Interface) : Interface by arg {
}

fun main() {
    println(">>>>>>>>>>>>> 委托访问")
    val classDemo = ClassDemo()
    val delegateDemo = DelegateDemo(classDemo)
    delegateDemo.f()
}