package cn.net.bhe.scalademo.control_demo

import scala.io.StdIn

/* Scala的关键字match类似Java的switch，但是Scala没有关键字switch。 */
object MatchDemo {
  def main(args: Array[String]): Unit = {
    var continue = true
    while (continue) {
      val in = StdIn.readLine()
      val ret: Int = in match {
        /* match确定的值 */
        case "1" => 1
        case "2" => 2
        case "3" => 3
        /* match范围内的值 */
        case i if i.length < 2 && i.forall(Character.isDigit) => i.toInt
        /* match类型：略。 */
        /* match对象：略。 */
        /* match集合：略。 */
        /* match未知 */
        case _ => {
          continue = false
          -1
        }
      }
      println(s"input = ${ret}")
    }
  }
}
