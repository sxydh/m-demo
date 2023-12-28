package cn.net.bhe

import cn.net.bhe.mutil.{DtUtils, FlUtils}

import java.io.{File, FileWriter}
import scala.collection.mutable.ListBuffer
import scala.util.Random

//noinspection SpellCheckingInspection,ScalaUnusedSymbol,ScalaWeakerAccess
package object sparksqldemo {

  val logSeq: Seq[(String, String, String, String)] = {
    val list = new ListBuffer[(String, String, String, String)]
    val random = new Random()
    val opArr = Array("INSERT", "UPDATE", "DELETE", "SELECT")
    for (it <- 1 to 10000) {
      list.append((DtUtils.ts17(), random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256), opArr(random.nextInt(opArr.length)), it.toString))
    }
    list
  }

  val logInput: String = {
    val dir = s"${FlUtils.getRootTmp}${File.separator}log_input"
    FlUtils.mkdir(dir)
    val file = new File(s"$dir${File.separator}00000000000000000.json")
    if (file.length() == 0) {
      var fileWriter: FileWriter = null
      try {
        fileWriter = new FileWriter(file, false)
        fileWriter.write("[")
        for (it <- logSeq.zipWithIndex) {
          if (it._2 != 0) {
            fileWriter.write(",")
          }
          fileWriter.write("{\"id\":\"" + it._1._1 + "\",\"ip\":\"" + it._1._2 + "\",\"op\":\"" + it._1._3 + "\",\"value\":\"" + it._1._4 + "\"}")
        }
        fileWriter.write("]")
      } finally {
        if (fileWriter != null) {
          fileWriter.close()
        }
      }
    }
    file.getAbsolutePath
  }

  val logHdfsInput: String = {
    "hdfs://hadoop01:8020/log_input"
  }

  val logOutput: String = {
    val dir = s"${FlUtils.getRootTmp}${File.separator}log_output"
    FlUtils.mkdir(dir)
    s"$dir${File.separator}${DtUtils.ts17}"
  }

}
