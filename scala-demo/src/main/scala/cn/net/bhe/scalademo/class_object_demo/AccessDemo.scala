package cn.net.bhe.scalademo.class_object_demo

import cn.net.bhe.mutil.StrUtils

object AccessDemo {

  /*
   * public：默认public，但是没有public关键字。
   */
  var public: String = StrUtils.HELLO_WORLD

  /*
   * protected：同类/子类可以访问。
   */
  protected var _protected: String = StrUtils.HELLO_WORLD

  /*
   * private[包名]：同类/内部类/伴生对象可以访问。包名可选，包名下的类也可以访问。
   */
  private class _private {}
}
