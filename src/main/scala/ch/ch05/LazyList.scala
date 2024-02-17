package ge.zgharbi.study.fps
package ch.ch05

import scala.annotation.targetName

enum LazyList[+A] {
  case Empty
  case Cons(h: () => A, t: () => LazyList[A])
}

object LazyList {
  def cons[A](hd: => A, tl: => LazyList[A]): LazyList[A] =
    lazy val head = hd
    lazy val tail = tl
    Cons(() => head, () => tail)

  extension [A](ll: LazyList[A])
    @targetName("cons")
    def :: = cons
}
