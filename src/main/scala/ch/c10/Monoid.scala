package ge.zgharbi.study.fps
package ch.c10

import ch.c07Parallelism.NonBlocking.Par

trait Monoid[A] {
  def combine(a1: A, a2: A): A
  def empty: A
}
object Monoid {
  def foldLeft[A, B](xs: IndexedSeq[A])(f: A => B)(using m: Monoid[B]): B = {
    if xs.isEmpty then m.empty
    else if xs.length == 1 then f(xs(0))
    else
      val (left, right) = xs.splitAt(xs.length / 2)
      m.combine(foldLeft(left)(f), foldLeft(right)(f))
  }

  def foldMapV[A, B](bs: IndexedSeq[A])(f: A => B)(using m: Monoid[B]): B = {
    if bs.isEmpty then m.empty
    else if bs.length == 1 then f(bs(0))
    else
      val (left, right) = bs.splitAt(bs.length / 2)
      m.combine(foldMapV(left)(f), foldMapV(right)(f))
  }

  given parMonoid[B](using monoid: Monoid[B]): Monoid[Par[B]] = par(monoid)

  def par[A](m: Monoid[A]): Monoid[Par[A]] = new Monoid[Par[A]] {
    def combine(a1: Par[A], a2: Par[A]): Par[A] = a1.map2(a2)(m.combine)

    def empty: Par[A] = Par.unit(m.empty)
  }

  def parFoldMap[A, B](as: IndexedSeq[A])(using monoid: Monoid[B])(f: A => B): Par[B] = {
    Par.parMap(as)(f).flatMap(bs => foldMapV(bs)(b => Par.lazyUnit(b)))
  }

  val stringMonoid: Monoid[String] = new:
    def combine(a1: String, a2: String): String = a1 + a2

    def empty: String = ""

  val intAddition: Monoid[Int] = new:
    def combine(a1: Int, a2: Int): Int = a1 + a2

    def empty: Int = 0

  val intMultiplication: Monoid[Int] = new:
    def combine(a1: Int, a2: Int): Int = a1 * a2

    def empty: Int = 1

  val booleanOr: Monoid[Boolean] = new:
    def combine(a1: Boolean, a2: Boolean): Boolean = a1 || a2

    def empty: Boolean = false

  val booleanAnd: Monoid[Boolean] = new:
    def combine(a1: Boolean, a2: Boolean): Boolean = a1 && a2

    def empty: Boolean = true

  def foldMap[A, B](as: List[A], m: Monoid[B])(f: A => B): B =
    as.foldLeft(m.empty)((b, a) => m.combine(b, f(a)))

  def dual[A](m: Monoid[A]): Monoid[A] = new:
    def combine(x: A, y: A): A = m.combine(y, x)

    def empty: A = m.empty

  def listMonoid[A]: Monoid[List[A]] = new:
    def combine(a1: List[A], a2: List[A]): List[A] = a1 ++ a2

    def empty: List[A] = Nil

  def optionMonoid[A](f: (A, A) => A): Monoid[Option[A]] = new:
    def combine(o1: Option[A], o2: Option[A]): Option[A] = o1.map2(o2)(f)

    def empty: Option[A] = None

  def endoMonoid[A]: Monoid[A => A] = new:
    def combine(f: A => A, g: A => A): A => A = f andThen g

    def empty: A => A = identity

  extension [A](kore: Option[A])
    infix def map2[B, C](sore: Option[B])(f: (A, B) => C): Option[C] =
      kore.flatMap(a => sore.map(b => f(a, b)))
}
