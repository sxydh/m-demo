package cn.net.bhe.kotlinqsdemo.exception_demo

fun main() {
    println(">>>>>>>>>>>>> 异常处理")
    try {
        println("执行：1 / 0")
        1 / "0".toInt()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        println("执行：finally")
    }
}