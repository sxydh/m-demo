package cn.net.bhe.scalademo.control_demo

import scala.language.postfixOps
import scala.util.control.Breaks.break

//noinspection ScalaUnusedSymbol
object _Main {

  def main(args: Array[String]): Unit = {

    ///////////// for /////////////
    for (i <- 1 to 10) {
    }
    for (i <- 1 until 10) {
    }
    for (i <- Array(1, 2, 3, 4)) {
    }
    // 有条件遍历（跳过 3）
    for (i <- 1 to 10 if i != 3) {
    }
    // 按步长遍历（13579）
    for (i <- 1 to 10 by 2) {
    }
    // 反转遍历
    for (i <- 1 to 10 reverse) {
    }
    // 嵌套遍历
    for (i <- 1 to 2; j <- 1 to 3) {
    }
    // 引入新变量
    for (i <- 1 to 2; j = i + 3) {
    }
    // 返回集合
    // Vector(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)
    val is = for (i <- 1 to 10) yield i * 2
    // 中断
    // break 不是关键字，其本质是 throw breakException。
    for (i <- 1 to 10) {
      if (i == 4) {
        break
      }
    }

    ///////////// match /////////////
    // varMatch 支持基本类型、字符串、集合、元组等。
    val varMatch = ""
    val varMatchRet = varMatch match {
      case "1" => 1
      case anyVarName if anyVarName.length <= 1 => 1
      case _ => -1 // 默认
    }
  }

}
