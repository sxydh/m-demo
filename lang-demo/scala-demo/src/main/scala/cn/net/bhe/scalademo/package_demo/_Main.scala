package cn.net.bhe.scalademo.package_demo

import cn.net.bhe.mutil.NumUtils

///////////// 第一层包 /////////////
//noinspection SpellCheckingInspection
package pkg {

  object pkgObj {
    val pkgObjInt: Integer = NumUtils.ONE
  }

  ///////////// 第二层包 /////////////
  package pkg2 {

    import cn.net.bhe.scalademo.package_demo.pkg.pkg2.pkg3.pkg3Obj

    object pkg2Obj {
      val pkg2ObjInt: Integer = NumUtils.TWO

      def main(args: Array[String]): Unit = {
        // 外部包访问内部包，需要显示引入内部包。
        println(pkg3Obj.pkg3ObjInt)
      }
    }

    ///////////// 第三层包 /////////////
    package pkg3 {

      object pkg3Obj {
        val pkg3ObjInt: Integer = NumUtils.THREE

        def main(args: Array[String]): Unit = {
          // 内部访问外部的包，不需要显示引入外部包。
          println(pkgObj.pkgObjInt)
          println(pkg2Obj.pkg2ObjInt)
        }
      }

    }

  }

}