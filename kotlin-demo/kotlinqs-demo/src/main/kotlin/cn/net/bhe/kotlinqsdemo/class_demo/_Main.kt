package cn.net.bhe.kotlinqsdemo.class_demo

/************ 类定义 ************/
class ClassDemo
/************ 主构造函数 ************/
// constructor 是可选的
// var 和 val 修饰的参数是类成员
constructor(
    var varInt: Int, // var 标识类成员 getter 和 setter，即可读可写。
    val valString: String, // val 标识类成员 getter，即只读。
    argLong: Long
) {
    /************ 成员属性 ************/
    // 成员属性必须初始化
    var varLong = argLong // 声明并初始化

    /************ 辅助构造函数 ************/
    // 辅助构造函数需要显示调用主构造函数
    constructor(argInt: Int) : this(argInt, "Hello World!", 1L) {
    }

    constructor(argInt: Int, argString: String) : this(argInt, argString, 1L) {
    }

    /************ 成员函数 ************/
    fun f() {
        println("ClassDemo.f() 被调用")
    }
}

/************ 数据类 ************/
// 编译器会自动给数据类提取以下函数：
// 1、equals() / hashCode()。
// 2、toString()，格式：User(name=John, age=42)。
// 3、copy()。
data class DataClassDemo(var varInt: Int, var varString: String) {}

fun main() {
    /************ 类访问 ************/
    println(">>>>>>>>>>>>> 常用类访问")
    val classDemo = ClassDemo(1)
    println("实例化 ClassDemo：varInt = ${classDemo.varInt}，valString = ${classDemo.valString}")
    classDemo.f()
    println()

    println(">>>>>>>>>>>>> 数据类访问")
    val dataClassDemo = DataClassDemo(1, "Hello World!")
    println("dataClassDemo.toString() = $dataClassDemo")
    println("调用数据类复制函数")
    val dataClassDemoCopy = dataClassDemo.copy(varInt = 11)
    println("dataClassDemoCopy.toString() = $dataClassDemoCopy")
}