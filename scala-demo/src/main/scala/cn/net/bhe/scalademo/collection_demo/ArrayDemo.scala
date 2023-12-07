package cn.net.bhe.scalademo.collection_demo

import scala.collection.mutable.ArrayBuffer

object ArrayDemo {}

/* 不可变数组 */
object ImmutableArrayDemo extends App {
  /* 创建未初始化的数组 */
  val strArr = new Array[String](10)
  println(strArr)

  /* 创建初始化的数组 */
  val strArr2 = Array("1", "2", "3")
  println(strArr2)

  /* 访问数组 */
  println(strArr2(0))

  /* 遍历数组 */
  for (i <- strArr2.indices) print(strArr2(i))
  println()
  for (e <- strArr2) print(e)
  println()
  val iterator = strArr2.iterator
  while (iterator.hasNext) {
    print(iterator.next())
  }
  println()

  /* 添加元素 */
  val strArr3 = strArr2 :+ "4"
  println(strArr2.mkString)
  println(strArr3.mkString)
}

/* 可变数组 */
object MutableArrayDemo extends App {
  /* 创建未初始化的数组 */
  val strArr = new ArrayBuffer[String](10)
  println(strArr)

  /* 创建初始化的数组 */
  val strArr2 = ArrayBuffer("1", "2", "3")
  println(strArr2)

  /* 访问数组 */
  println(strArr2(0))

  /* 修改数组 */
  strArr2.append("4")
  println(strArr2.mkString)
  strArr2.prepend("0")
  println(strArr2.mkString)
  strArr2(0) = "-"
  println(strArr2.mkString)

  /* 删除元素 */
  strArr2.remove(0)
  println(strArr2.mkString)
}
