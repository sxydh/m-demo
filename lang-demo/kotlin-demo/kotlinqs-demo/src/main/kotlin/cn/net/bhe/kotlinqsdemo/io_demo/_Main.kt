package cn.net.bhe.kotlinqsdemo.io_demo

import cn.net.bhe.mutil.DtUtils
import cn.net.bhe.mutil.FlUtils
import cn.net.bhe.mutil.StrUtils
import java.io.File
import java.io.FileOutputStream

/************ use ************/
// use 是一个用于处理可关闭资源（Closeable）的扩展函数
fun useDemo() {
    val dir = FlUtils.getRootTmp()
    FlUtils.mkdir(dir)
    val fileOutputStream = FileOutputStream("${dir}${File.separator}${DtUtils.ts17()}.txt")
    fileOutputStream.use {
        fileOutputStream.write(StrUtils.randomChs(100).toByteArray())
    }
}

fun main() {
    println(">>>>>>>>>>>>> useDemo")
    useDemo()
    println()
}