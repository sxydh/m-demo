package cn.net.bhe.scalademo

import cn.net.bhe.mutil.{NumUtils, StrUtils}

/* 包对象下声明的变量，可以作为该包下的共享变量。 */
package object class_object_demo {

  val i: Integer = NumUtils.ONE
  val str: String = StrUtils.HELLO_WORLD

}
