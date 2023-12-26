package cn.net.bhe.scalademo.collection_demo

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

//noinspection ScalaUnusedSymbol,ScalaUnusedExpression,ScalaWeakerAccess,DuplicatedCode,DfaConstantConditions
object _Main {

  ///////////// 不可变集合 /////////////
  // 长度不可变，内容可变。
  def immutableCollection(): Unit = {
    ///////////// Array /////////////
    val valIntArr: Array[Int] = Array(1, 2, 3)
    valIntArr(0)
    valIntArr(1)
    val valStringArr: Array[String] = Array("Hello", "World")
    for (i <- valStringArr.indices) {
    }

    ///////////// List /////////////
    val valIntList: List[Int] = List(1, 2, 3)
    valIntList.head // 1
    valIntList(1)
    valIntList.tail // List(2, 3)
    val valStringList: List[String] = List("Hello", "World")
    valStringList.head
    valStringList(1)
    valStringList.tail

    ///////////// Map /////////////
    val valMap: Map[Int, Int] = Map(1 -> 1, 2 -> 2)
    valMap(1)
    valMap.get(2).getOrElse()
    valMap.foreach(println)

    ///////////// Set /////////////
    val valSet: Set[Int] = Set(1, 2, 3)
    valSet.foreach(println)

    ///////////// Tuple /////////////
    val valTuple: (Int, String) = Tuple2(1, "2")
    valTuple._1
    valTuple._2
  }

  ///////////// 可变集合 /////////////
  // 长度可变，内容可变。
  def mutableCollection(): Unit = {
    ///////////// ArrayBuffer /////////////
    val valIntArrBuf: ArrayBuffer[Int] = ArrayBuffer()
    valIntArrBuf.append(-1)
    valIntArrBuf.append(1)
    valIntArrBuf.append(2)
    valIntArrBuf.append(3)
    valIntArrBuf.remove(0)
    valIntArrBuf.head // 1
    valIntArrBuf(1)
    valIntArrBuf.tail // ArrayBuffer(2, 3)
    val valStringArrBuf: ArrayBuffer[String] = ArrayBuffer("H", "e", "l", "l", "o")

    ///////////// ListBuffer /////////////
    val valIntListBuffer: ListBuffer[Int] = ListBuffer()
    valIntListBuffer.append(-1)
    valIntListBuffer.append(1)
    valIntListBuffer.append(2)
    valIntListBuffer.append(3)
    valIntListBuffer.remove(0)
    valIntListBuffer.head // 1
    valIntListBuffer(1)
    valIntListBuffer.tail // ListBuffer(2, 3)
    val valStringListBuffer: ListBuffer[String] = ListBuffer("Hello", "World", "!")

    ///////////// mutable.Map /////////////
    val valMutableMap: mutable.Map[Int, Int] = mutable.Map()
    valMutableMap.put(1, 1)
    valMutableMap.put(2, 2)
    valMutableMap(1)
    valMutableMap.get(2).getOrElse()

    ///////////// mutable.Set /////////////
    val valMutableSet: mutable.Set[Int] = mutable.Set()
    valMutableSet.add(1)
    valMutableSet.add(2)
    valMutableSet.add(3)
    valMutableSet.foreach(println)
  }

  def main(args: Array[String]): Unit = {
    immutableCollection()
    mutableCollection()
  }

}
