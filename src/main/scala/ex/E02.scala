package ge.zgharbi.study.fps
package ex

import scala.annotation.tailrec

object E02 {
  // 02.02
  @tailrec
  def isSorted[A](as: Array[A], cmp: (A, A) => Boolean): Boolean =
    as match
      case Array() | Array(_) => true
      case _ =>
        cmp(as(0), as(1)) && isSorted(as.tail, cmp)

  // 02.03
  def curry[A, B, C](f: (A, B) => C): A => (B => C) =
    a => b => f(a, b)

  // 02.04
  def uncurry[A, B, C](f: A => B => C): (A, B) => C =
    (a, b) => f(a)(b)

  // 02.05
  def compose[A, B, C](f: B => C, g: A => B): A => C =
    a => f(g(a))
}
