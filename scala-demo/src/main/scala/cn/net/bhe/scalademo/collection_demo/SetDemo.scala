package cn.net.bhe.scalademo.collection_demo

import scala.collection.mutable

object SetDemo {}

/* 不可变Set */
object ImmutableSetDemo extends App {
  /* 创建Set */
  val set = Set(1, 1, 2)
  println(set)

  /* 添加元素 */
  val set2 = set + 3
  println(set2)

  /* 合并Set */
  val set3 = set ++ set2
  println(set3)
}

/* 可变Set */
object MutableSetDemo extends App {
  /* 创建Set */
  val set = mutable.Set(1, 1, 2)
  println(set)

  /* 修改元素 */
  set.add(3)
  println(set)
}