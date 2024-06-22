@file:Suppress("USELESS_IS_CHECK", "CanBeVal", "UNUSED_VARIABLE", "RedundantExplicitType")

package cn.net.bhe.kotlinqsdemo.type_demo

fun main() {
    /************ 数字 ************/
    var varByte: Byte = 1 // 1个字节
    var varShort: Short = 1 // 2个字节
    var varInt: Int = 1 // 4个字节
    var varLong: Long = 1L // 8个字节

    var varFloat: Float = 1.1F // 4个字节
    var varDouble: Double = 1.1 // 8个字节

    /************ 布尔 ************/
    var varBoolean: Boolean = false // 1个字节

    /************ 字符 ************/
    var varChar: Char = 'a' // 2个字节

    /************ 字符串 ************/
    var varString: String = "Hello World!"

    /************ 数组 ************/
    var intArr: Array<Int> = arrayOf(1, 2, 3)
    var strArr: Array<String> = arrayOf("Hello", " ", "World", "!")

    /************ 无符号类型 ************/
    var uInt: UInt = 1U

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