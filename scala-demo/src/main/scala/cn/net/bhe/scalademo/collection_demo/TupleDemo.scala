package cn.net.bhe.scalademo.collection_demo

object TupleDemo extends App {
  /* 创建元组 */
  val tuple: (Int, String, Object) = (1, "1", new Object)
  println(tuple)

  /* 访问元组 */
  println(tuple._1)

  /* 遍历元组 */
  for (e <- tuple.productIterator) print(e)
  println()
}