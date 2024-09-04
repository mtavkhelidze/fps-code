package ge.zgharbi.study.fps
package ch.c10MonoidFoldable

import ch.c07Parallelism.NonBlocking.Par
import ch.c08Testing.exhaustive.{Gen, Prop}
import ch.c08Testing.Gen.**

trait Semigroup[A] {
    def combine(a1: A, a2: A): A
}
trait Monoid[A] extends Semigroup[A] {
  def empty: A
}

object Monoid {
  given stringMonoid: Monoid[String] = new Monoid[String]:
    def combine(a1: String, a2: String): String = a1 + a2

    def empty: String = ""

  def foldLeft[A, B](xs: IndexedSeq[A])(f: A => B)(using m: Monoid[B]): B = {
    if xs.isEmpty then m.empty
    else if xs.length == 1 then f(xs(0))
    else
      val (left, right) = xs.splitAt(xs.length / 2)
      m.combine(foldLeft(left)(f), foldLeft(right)(f))
  }

  def foldMapV[A, B](as: IndexedSeq[A])(f: A => B)(using m: Monoid[B]): B = {
    if as.isEmpty then m.empty
    else if as.length == 1 then f(as(0))
    else
      val (l, r) = as.splitAt(as.length / 2)
      m.combine(foldMapV(l)(f), foldMapV(r)(f))
  }

  given parMonoid[B](using monoid: Monoid[B]): Monoid[Par[B]] = par(monoid)

  private def par[A](m: Monoid[A]): Monoid[Par[A]] = new Monoid[Par[A]] {
    def combine(a1: Par[A], a2: Par[A]): Par[A] = a1.map2(a2)(m.combine)

    def empty: Par[A] = Par.unit(m.empty)
  }

  given intAddition: Monoid[Int] = new Monoid[Int]:
    def combine(a1: Int, a2: Int): Int = a1 + a2

    def empty: Int = 0
  val intMultiplication: Monoid[Int] = new Monoid[Int]:
    def combine(a1: Int, a2: Int): Int = a1 * a2

    def empty: Int = 1
  val booleanOr: Monoid[Boolean] = new Monoid[Boolean]:
    def combine(a1: Boolean, a2: Boolean): Boolean = a1 || a2

    def empty: Boolean = false

  val booleanAnd: Monoid[Boolean] = new Monoid[Boolean]:
    def combine(a1: Boolean, a2: Boolean): Boolean = a1 && a2

    def empty: Boolean = true

  def parFoldMap[A, B](
      as: IndexedSeq[A],
  )(f: A => B)(using monoid: Monoid[B]): Par[B] = {
    Par.parMap(as)(f).flatMap(bs => foldMapV(bs)(b => Par.lazyUnit(b)))
  }

  def dual[A](m: Monoid[A]): Monoid[A] = new Monoid[A]:
    def combine(x: A, y: A): A = m.combine(y, x)

    def empty: A = m.empty

  def foldMap[A, B](as: List[A], m: Monoid[B])(f: A => B): B =
    as.foldLeft(m.empty)((b, a) => m.combine(b, f(a)))

  def listMonoid[A]: Monoid[List[A]] = new Monoid[List[A]]:
    def combine(a1: List[A], a2: List[A]): List[A] = a1 ++ a2

    def empty: List[A] = Nil

  def optionMonoid[A](f: (A, A) => A): Monoid[Option[A]] =
    new Monoid[Option[A]]:
      def combine(o1: Option[A], o2: Option[A]): Option[A] = o1.map2(o2)(f)

      def empty: Option[A] = None

  def endoMonoid[A]: Monoid[A => A] = new Monoid[A => A]:
    def combine(f: A => A, g: A => A): A => A = f andThen g

    def empty: A => A = identity

  given functionMonoid[A, B](using mb: Monoid[B]): Monoid[A => B] with {
    override def combine(f: A => B, g: A => B): A => B =
      a => mb.combine(f(a), g(a))

    override def empty: A => B = _ => mb.empty
  }
  given mapMergeMonoid[K, V](using mv: Monoid[V]): Monoid[Map[K, V]] with {
    def combine(a: Map[K, V], b: Map[K, V]): Map[K, V] =
      (a.keySet ++ b.keySet).foldLeft(empty)((acc, k) =>
        acc.updated(
          k,
          mv.combine(a.getOrElse(k, mv.empty), b.getOrElse(k, mv.empty)),
        ),
      )

    def empty: Map[K, V] = Map.empty
  }

  given productMonoid[A, B](using ma: Monoid[A], mb: Monoid[B]): Monoid[(A, B)]
  with {
    override def combine(a1: (A, B), a2: (A, B)): (A, B) =
      (ma.combine(a1._1, a2._1), mb.combine(a1._2, a2._2))

    override def empty: (A, B) = (ma.empty, mb.empty)
  }

  extension [A](kore: Option[A])
    infix def map2[B, C](sore: Option[B])(f: (A, B) => C): Option[C] =
      kore.flatMap(a => sore.map(b => f(a, b)))

  def monoidLaws[A](m: Monoid[A], gen: Gen[A]): Prop =
    val associativity = Prop
      .forAll(gen ** gen ** gen):
        case a ** b ** c =>
          m.combine(a, m.combine(b, c)) == m.combine(m.combine(a, b), c)
      .tag("associativity")
    val identity = Prop
      .forAll(gen): a =>
        m.combine(a, m.empty) == a && m.combine(m.empty, a) == a
      .tag("identity")
    associativity && identity

  def bag[A](as: IndexedSeq[A]): Map[A, Int] = {
    import Foldable.given
    as.foldMap(a => Map(a -> 1))
  }
}

enum WC {
  case Stub(chars: String)
  case Part(lStub: String, words: Int, rStub: String)
}

object WC {
  val monoid: Monoid[WC] = new Monoid[WC] {

    override def combine(a1: WC, a2: WC): WC = (a1, a2) match
      case (Stub(a), Stub(b)) => WC.Stub(a + b)
      case (Stub(a), WC.Part(l, w, r)) => Part(a + l, w, r)
      case (Part(l, w, r), Stub(a)) => WC.Part(l, w, r + a)
      case (Part(l, w, r), Part(l2, w2, r2)) =>
        Part(l, w + (if (r + l2).isEmpty then 0 else 1) + w2, r2)

    override def empty: WC = Stub("")
  }

  def wcGen: Gen[WC] = {
    val smallString = Gen.choose(0, 20).flatMap(Gen.stringN)
    val stubGen: Gen[WC] = smallString.map(WC.Stub.apply)
    val partGen: Gen[WC] = for {
      lStub <- smallString
      words <- Gen.choose(0, 10)
      rStub <- smallString
    } yield WC.Part(lStub, words, rStub)
    Gen.union(stubGen, partGen)
  }

  def count(s: String): Int = {
    def unStub(s: String): Int = if s.isEmpty then 0 else 1

    given wcMonoid: Monoid[WC] = monoid

    Monoid.foldMapV(s.toIndexedSeq)(wc) match {
      case WC.Part(lStub, w, rStub) => unStub(lStub) + w + unStub(rStub)
      case WC.Stub(chars) => unStub(chars)
    }
  }

  private def wc(c: Char): WC = {
    if c.isWhitespace then WC.Part("", 0, "")
    else WC.Stub(c.toString)
  }
}
