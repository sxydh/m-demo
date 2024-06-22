package cn.net.bhe.scalademo.function_demo

import cn.net.bhe.mutil.{NumUtils, StrUtils}

import scala.language.implicitConversions

//noinspection ScalaWeakerAccess,ProcedureDefinition,TypeAnnotation,ScalaUnusedSymbol
object _Main {

  ///////////// 函数参数 /////////////
  // 空参
  def argFun(): Unit = println()

  // 一个参数
  def argFun2(i: Int): Unit = println(i)

  // 多个参数
  def argFun3(i: Int, str: String): Unit = println(s"$i, $str")

  // 参数带默认值
  def argFun4(i: Int = NumUtils.ONE, str: String = StrUtils.HELLO_WORLD): Unit = println(s"$i, $str")

  // 可变参数
  def argFun5(strArr: String*): Unit = println(strArr)

  // 参数是函数
  def argFun6(any: String => Unit, str: String): Unit = any.apply(str)

  ///////////// 函数返回值 /////////////
  //  没有返回类型
  def retFun(): Unit = {}

  //  没有返回类型简化
  def retFun2() {}

  // 显示声明返回类型
  def retFun3(): Int = NumUtils.ONE

  // 返回类型省略（前提是类型可以推导）
  def retFun4() = NumUtils.ONE

  // 返回类型是函数
  def retFun5(): Any => Unit = {
    def myPrintln(any: Any) = {
      System.out.println(any)
    }

    myPrintln // 默认最后一行作为返回值
  }

  ///////////// Lambda 表达式 /////////////
  def lambdaFun(): Unit = {
    // 作为变量
    val lambdaPrint = (any: Any) => System.out.println(any)
    lambdaPrint.apply(StrUtils.randomChs(6))

    // 作为参数
    def agg(sum: (Int, Int) => Int, a: Int, b: Int): Int = {
      sum.apply(a, b)
    }

    agg(_ + _, NumUtils.ranInt(), NumUtils.ranInt()) // 如果参数只用到一次，则可以用通配符 _ 代替，并省略参数列表。
  }

  ///////////// 传名参数 /////////////
  // 1、传名参数是一种参数传递的方式，其特点是在调用时不会立即求值，而是在被调用的地方进行延迟求值。
  // 2、传名参数使用 => 符号来定义
  def byNameFun(argInt: => Int): Unit = {
    // argInt 计算过程会被执行两次
    println(argInt + argInt)
  }

  ///////////// 函数柯里化 /////////////
  // 1、函数柯里化（Currying）是指将接受多个参数的函数转化为一系列接受单一参数的函数的过程，即将一个函数从可调用的f(a, b, c)转换为可调用的f(a)(b)(c)。
  // 2、函数柯里化是一种常见的编程技巧，它允许你创建更灵活、更模块化的函数。
  // 3、柯里化的函数可以让你在部分应用参数的同时，留下剩余参数，或者更容易地复用部分应用的函数。
  // 4、柯里化实现用到了闭包，所谓闭包就是：如果一个函数，访问到了它的外部（局部）变量的值，那么这个函数和它所处的环境，称为闭包。
  def curryingFun(): Unit = {
    // 非柯里化的函数
    def add(x: Int, y: Int): Int = x + y

    add(1, 2)

    // 柯里化的函数
    def addCurried(x: Int)(y: Int): Int = x + y

    val addOne = addCurried(1) _
    addOne(2)
    addOne(3)
  }

  ///////////// lazy 修饰符 /////////////
  // 1、用于延迟初始化某个值，直到该值被首次访问时才进行计算。
  // 2、通过使用 lazy 修饰符，可以避免在对象初始化时就计算可能不会被用到的值，从而提高程序的性能和效率。
  def lazyFun(): Unit = {
    def expensiveOperation(): Int = {
      println("expensiveOperation begin")
      Thread.sleep(1000)
      val ret = NumUtils.ranInt()
      println(s"expensiveOperation end = $ret")
      ret
    }

    lazy val eoRes = expensiveOperation()
    println(s"eoRes = ")
    println(eoRes) // 触发计算
    println(s"eoRes = ")
    println(eoRes) // 第二次直接复用第一次的返回值

    // 输出
    // eoRes =
    // expensiveOperation begin
    // expensiveOperation end = -418074109
    // -418074109
    // eoRes =
    // -418074109
  }

  ///////////// 隐式函数 /////////////
  def implicitFun(): Unit = {
    ///////////// 隐式转换函数 /////////////
    // 定义一个隐式转换函数
    implicit def doubleToInt(d: Double): Int = d.toInt

    // 编译器会自动调用 doubleToInt 将 Double 转换为 Int
    val x: Int = 3.5

    ///////////// 隐式参数 /////////////
    // 定义一个带有隐式参数的函数
    def greet(name: String)(implicit greeting: String): Unit = {
      println(s"$greeting, $name!")
    }

    // 定义一个隐式值
    // 隐式值优先级比参数默认值高
    implicit val defaultGreeting: String = "Hello"
    // 调用带有隐式参数的函数
    // 输出
    // Hello, Alice!
    greet("Alice")

    ///////////// 隐式类 /////////////
    // 隐式类不能作为顶层类
    // 定义一个隐式类
    implicit class StringOps(s: String) {
      def exclaim: String = s + "!"
    }

    // 使用隐式类
    val greeting: String = "Hello"
    // 编译器会自动调用 StringOps 类的 exclaim 方法
    val excitedGreeting: String = greeting.exclaim
  }

  ///////////// 偏函数 /////////////
  // 偏函数是指对于某些输入值有定义，而对于其他输入值则没有定义的函数。
  def partialFunction(): Unit = {
    val divide: PartialFunction[Int, Int] = {
      case x if x != 0 => 42 / x // 只对 x 不为 0 有定义
    }
  }

  def main(args: Array[String]): Unit = {
    argFun()
    argFun2(NumUtils.ranInt())
    argFun3(NumUtils.ranInt(), StrUtils.randomChs(6))
    argFun4()
    argFun5(StrUtils.randomChs(6), StrUtils.randomChs(6))
    argFun6(println, StrUtils.randomChs(6))

    println(retFun())
    println(retFun2())
    println(retFun3())
    println(retFun4())
    println(retFun5())

    lambdaFun()

    byNameFun({
      val i = NumUtils.ranInt()
      println(i)
      i
    })

    curryingFun()

    lazyFun()

    implicitFun()
  }
}