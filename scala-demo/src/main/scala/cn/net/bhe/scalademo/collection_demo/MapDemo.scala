package cn.net.bhe.scalademo.collection_demo

import scala.collection.mutable

object MapDemo {}

/* 不可变Map */
object ImmutableMapDemo extends App {
  /* 创建Map */
  val map: Map[String, String] = Map("k1" -> "v2", "k2" -> "v2")
  println(map)

  /* 访问Map */
  println(map.get("k1").getClass)
  println(map("k1"))

  /* 遍历Map */
  map.foreach(print)
  println()
}

/* 可变Map */
object MutableMapDemo extends App {
  /* 创建Map */
  val map: mutable.Map[String, String] = mutable.Map("k1" -> "v2", "k2" -> "v2")
  println(map)

  /* 修改Map */
  map.put("k3", "v3")
  println(map)
}