package ge.zgharbi.study.fps
package ex

import scala.annotation.tailrec

object E02 {
  @tailrec
  def isSorted[A](as: Array[A], cmp: (A, A) => Boolean): Boolean =
    as match
      case Array() | Array(_) => true
      case _ =>
        cmp(as(0), as(1)) && isSorted(as.tail, cmp)
}
