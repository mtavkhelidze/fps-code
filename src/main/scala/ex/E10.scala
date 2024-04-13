package ge.zgharbi.study.fps
package ex

import ch.c10.Monoid

import scala.collection

object E10 {
  def foldLeft[A, B](xs: IndexedSeq[A])(f: A => B)(using m: Monoid[B]): B =
    if xs.isEmpty then m.empty
    else if xs.length == 1 then f(xs(0))
    else
      val (left, right) = xs.splitAt(xs.length / 2)
      m.combine(foldLeft(left)(f), foldLeft(right)(f))
}
