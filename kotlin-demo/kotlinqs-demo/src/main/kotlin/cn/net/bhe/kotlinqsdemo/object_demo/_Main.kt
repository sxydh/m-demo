package cn.net.bhe.kotlinqsdemo.object_demo

open class ClassDemo {
    fun f() {
        println("ClassDemo.f() 被调用")
    }
}

/************ 对象表达式 ************/
// 对象表达式即匿名类
// 可以用来扩展父类的功能
fun anonymousObjectDemo() {
    val valObject = object : ClassDemo() {
        fun f2() {
            println("valObject.f2() 被调用")
        }
    }
    valObject.f()
    valObject.f2()
}

/************ 对象声明 ************/
// 可以用作单例
object ObjectDemo {
    fun f() {
        println("ObjectDemo.f() 被调用")
    }
}

fun main() {
    println(">>>>>>>>>>>>> 对象表达式")
    anonymousObjectDemo()
    println()

    println(">>>>>>>>>>>>> 对象声明")
    ObjectDemo.f()
}