@file:Suppress("FunctionName", "LocalVariableName")

package cn.net.bhe.kotlinqsdemo.fun_demo

/************ 函数的参数 ************/
// 空参
fun argDemo_none() {
}

// 有参
// 参数类型必须显示定义
fun argDemo(argInt: Int, argString: String) {
}

// 参数默认值
fun argDemo_default(argInt: Int = 1, argString: String = "Hello World!") {
}

// 可变参数
fun argDemo_vararg(vararg argInt: Int) {
}

// 参数是函数
fun argDemo_fun(argAgg: (Int, Int) -> Int, argInt: Int, argInt2: Int): Int {
    return argAgg.invoke(argInt, argInt2)
}

/************ 函数的返回类型 ************/
// 没有返回类型
// Unit是可选的
fun returnDemo_Unit(): Unit {
    return Unit
}

// 显示返回类型
fun returnDemo_Int(): Int {
    return 1
}

/************ 局部函数 ************/
fun scopeFunDemo() {
    fun f(): Int {
        return 1
    }

    val f2 = fun(): String {
        return "Hello World!"
    }
    f()
    f2()
}

/************ Lambda表达式 ************/
fun lambdaFunDemo() {
    val f = { a: Int, b: Int -> a + b }
    // 表达式隐式返回最后一个值
    val valInt = f(1, 1)
}

/************ 扩展函数 ************/
// 可以扩展类的功能
class ClassDemo {}

fun ClassDemo.f() {
    println("ClassDemo.f() 被调用")
}

fun main() {
    /************ 函数访问 ************/
    println(">>>>>>>>>>>>> 函数的参数")
    argDemo_none()
    argDemo(1, "Hello World!")
    argDemo_default(argInt = 2)
    argDemo_vararg(1, 1, 1)
    argDemo_fun({ a, b -> a + b }, 1, 1)
    println()

    println(">>>>>>>>>>>>> 函数的返回类型")
    returnDemo_Unit()
    returnDemo_Int()
    println()

    println(">>>>>>>>>>>>> 局部函数")
    scopeFunDemo()
    println()

    println(">>>>>>>>>>>>> Lambda表达式")
    lambdaFunDemo()
    println()

    println(">>>>>>>>>>>>> 扩展函数")
    val classDemo = ClassDemo()
    classDemo.f()
    println()
}