package cn.net.bhe.scalademo.collection_demo

import scala.collection.mutable.ListBuffer

object ListDemo {}

/* 不可变列表 */
object ImmutableListDemo extends App {
  /* 创建列表 */
  val list = List(1, 2, 3)
  println(list)

  /* 访问列表 */
  println(list(2))
  for (e <- list) print(e)
  println()

  /* 添加元素 */
  val list2 = list :+ 4
  println(list2)

  /* 合并列表 */
  val list3 = list ::: list2
  println(list3)
}

/* 可变列表 */
object MutableListDemo extends App {
  /* 创建列表 */
  val list = ListBuffer(1, 2, 3)
  println(list)

  /* 访问列表 */
  println(list(2))
  for (e <- list) print(e)
  println()

  /* 修改元素 */
  list(1) = 1
  println(list)
}