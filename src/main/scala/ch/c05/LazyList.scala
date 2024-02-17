package ge.zgharbi.study.fps
package ch.c05

import scala.annotation.tailrec

enum LazyList[+A] {
  case Empty
  case Cons(h: () => A, t: () => LazyList[A])
}

object LazyList {
  def cons[A](hd: => A, tl: => LazyList[A]): LazyList[A] =
    lazy val head = hd
    lazy val tail = tl
    Cons(() => head, () => tail)

  def apply[A](as: A*): LazyList[A] =
    if as.isEmpty then empty
    else cons(as.head, apply(as.tail*))

  def empty[A]: LazyList[A] = LazyList.Empty

  extension [A](ll: LazyList[A])
    def headOption: Option[A] = ll match {
      case Empty      => None
      case Cons(h, _) => Some(h())
    }
    def toList: List[A] =
      @tailrec
      def go(l: LazyList[A], acc: List[A]): List[A] = l match
        case LazyList.Empty      => acc.reverse
        case LazyList.Cons(h, t) => go(t(), h() :: acc)
      go(ll, Nil)

    def take(n: Int): LazyList[A] = ll match {
      case LazyList.Cons(h, t) if n > 1  => cons(h(), t().take(n - 1))
      case LazyList.Cons(h, _) if n == 1 => cons(h(), empty)
      case _                             => empty
    }

    @tailrec
    def drop(n: Int): LazyList[A] = ll match
      case LazyList.Cons(_, t) if n > 0 => t().drop(n - 1)
      case _                            => ll
}
