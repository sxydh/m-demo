package cn.net.bhe.scalademo.io_demo

import cn.net.bhe.mutil.FlUtils

import java.io.File
import scala.io.Source

object SourceDemo {
  def main(args: Array[String]): Unit = {
    val path = FlUtils.getRootTmp + File.separator + PrintWriterDemo.getClass.getSimpleName
    val source = Source.fromFile(s"${path}.txt")
    source.foreach(print)
    source.close()
  }
}
