@file:Suppress("USELESS_IS_CHECK", "CanBeVal")

package cn.net.bhe.kotlinqsdemo.type_demo

fun main() {
    /************ 数字 ************/
    var varByte = 1 // 1个字节
    var varShort = 1 // 2个字节
    var varInt = 1 // 4个字节
    var varLong = 1L // 8个字节

    var varFloat = 1.1F // 4个字节
    var varDouble = 1.1 // 8个字节

    /************ 布尔 ************/
    var varBoolean = false // 1个字节

    /************ 字符 ************/
    var varChar = 'a' // 2个字节

    /************ 字符串 ************/
    var varString = "Hello World!"

    /************ 数组 ************/
    var intArr = arrayOf(1, 2, 3)
    var strArr = arrayOf("Hello", " ", "World", "!")

    /************ 无符号类型 ************/
    var uInt = 1U

    /************ 类型检测 ************/
    if (varInt is Int) {
        println("varInt 检测是 Int")
    }
    /************ 智能转换 ************/
    if (varString is String) {
        // 自动转为 String
        println("varString 检测是 String，长度为：" + varString.length)
    }
}