@file:Suppress("JoinDeclarationAndAssignment", "KotlinConstantConditions")

package cn.net.bhe.kotlinqsdemo.var_demo

fun varDemo() {
    /************ 变量 ************/
    // 类型是可选的
    var varInt: Int = 1
    var varString: String = "Hello World!"

    /************ 常量 ************/
    // 类型是可选的
    val valInt: Int
    val valString: String

    // 常量可以先声明再初始化，但是只能初始化一次。
    valInt = 1
    valString = "Hello World!"
}

/************ 空安全 ************/
fun npeDemo() {
    // 编译器不允许 null 变量，除非显示用 ? 修饰。
    val varString: String? = null
    // 访问可能为 null 的变量需要用 ? 修饰
    var strLen = varString?.length

    // 非空检查可以用标识符 !!
    strLen = varString!!.length
}

fun main() {
    println(">>>>>>>>>>>>> varDemo")
    varDemo()
    println()

    println(">>>>>>>>>>>>> npeDemo")
    npeDemo()
    println()
}