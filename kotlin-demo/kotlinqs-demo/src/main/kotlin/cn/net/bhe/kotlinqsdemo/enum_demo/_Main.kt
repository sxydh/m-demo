package cn.net.bhe.kotlinqsdemo.enum_demo

/************ 枚举定义 ************/
enum class Direction {
    NORTH, SOUTH, WEST, EAST
}

enum class Color(val rgb: Int) {
    // 属性初始化
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF)
}

fun main() {
    println(">>>>>>>>>>>>> 无参初始化")
    val north: Direction = Direction.NORTH
    println(north)
    println()

    println(">>>>>>>>>>>>> 参数初始化")
    val blue: Color = Color.BLUE
    println("blue.rgb = ${blue.rgb}")
}