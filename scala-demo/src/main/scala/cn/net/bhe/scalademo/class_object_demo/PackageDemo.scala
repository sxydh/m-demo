package cn.net.bhe.scalademo.class_object_demo

import cn.net.bhe.mutil.NumUtils

/* 第一层包 */
package pkg {

  object pkgObj {
    val pkgi: Integer = NumUtils.ONE
  }

  /* 第二层包 */
  package pkg2 {

    import cn.net.bhe.scalademo.class_object_demo.pkg.pkg2.pkg3.pkg3Obj

    object pkg2Obj {
      val pkg2i: Integer = NumUtils.TWO

      def main(args: Array[String]): Unit = {
        /* 外部包访问内部包，需要显示引入内部包。 */
        println(pkg3Obj.pkg3i)
      }
    }

    /* 第三层包 */
    package pkg3 {

      object pkg3Obj {
        val pkg3i: Integer = NumUtils.THREE

        def main(args: Array[String]): Unit = {
          /* 内部访问外部的包，不需要显示引入外部包。 */
          println(pkgObj.pkgi)
          println(pkg2Obj.pkg2i)
        }
      }

    }

  }

}