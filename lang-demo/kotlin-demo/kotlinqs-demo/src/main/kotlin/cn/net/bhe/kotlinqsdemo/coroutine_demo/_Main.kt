@file:Suppress("OPT_IN_USAGE")

package cn.net.bhe.kotlinqsdemo.coroutine_demo

import cn.net.bhe.mutil.StrUtils
import kotlinx.coroutines.*

/************ 协程 ************/
// Kotlin 仅在标准库中提供最基本底层 API 以便其他库能够利用协程。
// 与许多其他具有类似功能的语言不同，async 与 await 在 Kotlin 中并不是关键字，甚至都不是标准库的一部分。

/************ runBlocking 构建器 ************/
// 1、创建并执行一个新的协程
// 2、协程的执行会阻塞调用者线程
// 3、协程执行期间不可取消，直至执行完毕。
suspend fun runBlockingDemo() {
    runBlocking {
        println("[${Thread.currentThread().name}] 协程 1.1 已执行")
        delay(1000)
        println("[${Thread.currentThread().name}] 协程 1.2 已执行")
    }
    val job = GlobalScope.async {
        runBlocking {
            println("[${Thread.currentThread().name}] 协程 2.1 已执行")
            delay(1000)
            println("[${Thread.currentThread().name}] 协程 2.2 已执行")
        }
    }
    delay(500)
    job.cancel()
    job.join()
}

/************ coroutineScope 构建器 ************/
// 1、创建并执行一个新的协程
// 2、协程执行不会阻塞调用者线程。协程 suspend 时会让整个作用域的 suspend fun 都处于 suspend 状态，这得以让调用者（不是 suspend fun）线程继续执行。
// 3、协程执行期间可以取消
suspend fun coroutineScopeDemo() {
    coroutineScope {
        println("[${Thread.currentThread().name}] 协程 1.1 已执行")
        delay(1000)
        println("[${Thread.currentThread().name}] 协程 1.2 已执行")
    }
    val job = GlobalScope.async {
        coroutineScope {
            println("[${Thread.currentThread().name}] 协程 2.1 已执行")
            delay(1000)
            println("[${Thread.currentThread().name}] 协程 2.2 已执行")
        }
    }
    delay(500)
    job.cancel()
    job.join()
}

/************ launch 构建器 ************/
// 启动一个无返回值的协程
suspend fun launchDemo() {
    coroutineScope {
        // job 没有值
        val job = launch {
            println("[${Thread.currentThread().name}] 协程 1.1 已执行")
            delay(1000)
            println("[${Thread.currentThread().name}] 协程 1.2 已执行")
        }
        println("[${Thread.currentThread().name}] 协程 2 已执行")
        job
    }
}

/************ async 构建器 ************/
// 启动一个有返回值的协程
suspend fun asyncDemo() {
    coroutineScope {
        // deferred 有值
        val deferred: Deferred<String> = async {
            println("[${Thread.currentThread().name}] 协程 1.1 已执行")
            delay(1000)
            println("[${Thread.currentThread().name}] 协程 1.2 已执行")
            StrUtils.randomNum(6)
        }
        println("[${Thread.currentThread().name}] 协程 1 返回值 = ${deferred.await()}")
        println("[${Thread.currentThread().name}] 协程 2 已执行")
    }
}

suspend fun main() {
    println(">>>>>>>>>>>>> runBlockingDemo")
    runBlockingDemo()
    println()

    println(">>>>>>>>>>>>> coroutineScopeDemo")
    coroutineScopeDemo()
    println()

    println(">>>>>>>>>>>>> launchDemo")
    launchDemo()
    println()

    println(">>>>>>>>>>>>> asyncDemo")
    asyncDemo()
    println()
}