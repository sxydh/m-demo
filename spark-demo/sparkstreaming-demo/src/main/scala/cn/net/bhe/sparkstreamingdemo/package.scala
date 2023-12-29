package cn.net.bhe

import cn.net.bhe.mutil.{DtUtils, FlUtils}
import org.apache.spark.rdd.RDD

import java.io.File
import scala.collection.mutable

//noinspection SpellCheckingInspection,ScalaUnusedSymbol
package object sparkstreamingdemo {

  val queue = new mutable.Queue[RDD[String]]()

  val wdOutput: String = {
    val dir = s"${FlUtils.getRootTmp}${File.separator}wd_output"
    FlUtils.mkdir(dir)
    s"$dir${File.separator}${DtUtils.ts17}"
  }

}
