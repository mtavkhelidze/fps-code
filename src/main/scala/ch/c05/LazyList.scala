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

  def unfold[S, A](state: S)(f: S => Option[(S, A)]): LazyList[A] =
    f(state) match
      case None         => empty
      case Some((s, a)) => cons(a, unfold(s)(f))

  def continually[A](a: A): LazyList[A] =
    unfold(a)(o => Some((o, a)))

  def from(n: Int): LazyList[Int] =
    unfold(n)(pv => Some(pv + 1, pv))
    
  extension [A](self: LazyList[A]) {
    def headOption: Option[A] =
      self.foldRight(None: Option[A])((a, _) => Some(a))

    def toList: List[A] =
      @tailrec
      def go(l: LazyList[A], acc: List[A]): List[A] = l match
        case LazyList.Empty      => acc.reverse
        case LazyList.Cons(h, t) => go(t(), h() :: acc)

      go(self, Nil)

    def take(n: Int): LazyList[A] = self match {
      case LazyList.Cons(h, t) if n > 1  => cons(h(), t().take(n - 1))
      case LazyList.Cons(h, _) if n == 1 => cons(h(), empty)
      case _                             => empty
    }

    @tailrec
    def drop(n: Int): LazyList[A] = self match
      case LazyList.Cons(_, t) if n > 0 => t().drop(n - 1)
      case _                            => self

    def takeWhile(p: A => Boolean): LazyList[A] =
      self.foldRight(empty)((a, acc) => if p(a) then cons(a, acc) else acc)

    def exists(p: A => Boolean): Boolean =
      self.foldRight(false)((a, acc) => p(a) || acc)

    def foldRight[B](acc: => B)(f: (A, B) => B): B = self match
      case LazyList.Empty      => acc
      case LazyList.Cons(h, t) => f(h(), t().foldRight(acc)(f))

    def forAll[B](p: A => Boolean): Boolean =
      self.foldRight(true)((a, acc) => acc && p(a))

    def map[B](f: A => B): LazyList[B] =
      self.foldRight(empty)((a, acc) => cons(f(a), acc))

    def filter(f: A => Boolean): LazyList[A] =
      self.foldRight(empty)((a, acc) => if f(a) then cons(a, acc) else acc)

    def append[AA >: A](that: => LazyList[AA]): LazyList[AA] =
      self.foldRight(that)((a, acc) => cons(a, acc))

    def flatMap[B](f: A => LazyList[B]): LazyList[B] =
      self.foldRight(empty[B])((a, acc) => f(a).append(acc))

    def find(p: A => Boolean): Option[A] = self.filter(p).headOption
  }
}

object LazyListOps {}
