package cn.net.bhe.scalademo.io_demo

import cn.net.bhe.mutil.{FlUtils, StrUtils}

import java.io.{File, PrintWriter}

class PrintWriterDemo {}

object PrintWriterDemo {
  def main(args: Array[String]): Unit = {
    var path = FlUtils.getRootTmp
    FlUtils.mkdir(path)
    path = path + File.separator + PrintWriterDemo.getClass.getSimpleName
    val writer = new PrintWriter(new File(s"${path}.txt"))
    writer.write(StrUtils.randomChs(6))
    writer.flush()
    writer.close()
  }
}
