@file:Suppress("RedundantIf")

package cn.net.bhe.kotlinqsdemo.control_demo

fun ifElseDemo(inputLine: String?) {
    /************ 基本语法 ************/
    var line = inputLine
    if (line != null) {
        if (line.length < 3) {
            println("输入长度小于 3")
        } else if (line.length < 7) {
            println("输入长度小于 7")
        } else {
            println("输入长度无法处理")
        }
    } else {
        line = "1"
    }
    println("输入为：${line}")

    /************ 表达式作为返回值 ************/
    val isNum = if (line.all { char -> char.isDigit() }) true else false
    println("输入是数值：${isNum}")

    /************ 表达式使用区间 ************/
    val lineToInt = line.toInt()
    if (lineToInt in 1..100) {
        println("输入值在区间内 [1, 100]")
    }
}

fun whenDemo(inputLine: String?) {
    when (inputLine) {
        "1" -> println("输入为：1")
        "2" -> println("输入为：2")
        else -> println("输入不识别")
    }
}

fun forDemo(inputLine: String) {
    val arr = (1..inputLine.toInt()).toList()

    print("for in 不带下标遍历：")
    for (e in arr) {
        if (e == 3) {
            print("遇到 3 跳过，")
            continue
        } else if (e == 7) {
            print("遇到 7 终止，")
            break
        }
        print("${e}，")
    }
    println()

    print("for in 带下标遍历：")
    for ((i, e) in arr.withIndex()) {
        print("charArr[${i}]=${e}，")
    }
    println()
}

fun whileDemo(inputLine: String) {
    val inputInt = inputLine.toInt()

    print("while 遍历：")
    var i = 0
    while (++i <= inputInt) {
        print("${i}，")
    }
}

fun main() {
    val inputLine = readlnOrNull()
    inputLine!!

    println(">>>>>>>>>>>>> ifElseDemo")
    ifElseDemo(inputLine)
    println()

    println(">>>>>>>>>>>>> whenDemo")
    whenDemo(inputLine)
    println()

    println(">>>>>>>>>>>>> forDemo")
    forDemo(inputLine)
    println()

    println(">>>>>>>>>>>>> whileDemo")
    whileDemo(inputLine)
    println()
}