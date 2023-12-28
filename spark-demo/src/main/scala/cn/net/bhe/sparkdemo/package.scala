package cn.net.bhe

import cn.net.bhe.mutil.{DtUtils, FlUtils}

import java.io.{File, FileWriter}
import scala.collection.mutable.ListBuffer
import scala.util.Random

//noinspection SpellCheckingInspection,ScalaUnusedSymbol,ScalaWeakerAccess
package object sparkdemo {

  val intSeq: Seq[Int] = 1 to 100

  val intOutput: String = {
    val dir = s"${FlUtils.getRootTmp}${File.separator}int_output"
    FlUtils.mkdir(dir)
    s"$dir${File.separator}${DtUtils.ts17}"
  }

  val wdSeq: Seq[String] = {
    val wordArr = "Apache Spark™ is a multi-language engine for executing data engineering, data science, and machine learning on single-node machines or clusters.".replaceAll("[,.]", "").split(" ")
    val listBuffer = new ListBuffer[String]
    val random = new Random
    for (i <- 1 to 10000) {
      val start = random.nextInt(wordArr.length)
      val len = Math.max(random.nextInt(wordArr.length - start), 1)
      val copyArr: Array[String] = Array.ofDim(len)
      System.arraycopy(wordArr, start, copyArr, 0, len)
      listBuffer.append(copyArr.mkString(" "))
    }
    listBuffer
  }

  val wdInput: String = {
    val dir = s"${FlUtils.getRootTmp}${File.separator}wd_input"
    FlUtils.mkdir(dir)
    val file = new File(s"$dir${File.separator}00000000000000000.txt")
    if (file.length() == 0) {
      var fileWriter: FileWriter = null
      try {
        fileWriter = new FileWriter(file, false)
        for (item <- wdSeq) {
          fileWriter.write(item)
          fileWriter.write(System.lineSeparator)
        }
      } finally {
        if (fileWriter != null) {
          fileWriter.close()
        }
      }
    }
    file.getAbsolutePath
  }

  val wdHdfsInput: String = {
    "hdfs://hadoop01:8020/tmp"
  }

}
