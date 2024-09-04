package ge.zgharbi.study.fps
package ch.c12Applicative

import ch.c10MonoidFoldable.{Monoid, Semigroup}
import ch.c11Monad.Functor

object Applicative {
  opaque type ZipList[+A] = LazyList[A]

  enum Validated[+E, +A] {
    case Valid(get: A) extends Validated[Nothing, A]
    case Invalid(error: E) extends Validated[E, Nothing]
  }

  case class NonEmptyList[+A](head: A, tail: List[A]) {
    def toList: List[A] = head :: tail
  }

  object NonEmptyList {
    def apply[A](head: A, tail: A*): NonEmptyList[A] =
      NonEmptyList(head, tail.toList)

    given nelSemigroup[A]: Semigroup[NonEmptyList[A]] with {
      override def combine(
          x: NonEmptyList[A],
          y: NonEmptyList[A],
      ): NonEmptyList[A] =
        NonEmptyList(x.head, x.tail ++ y.toList)
    }
  }
  object Validated {

    given validatedApplicative[E: Semigroup]: Applicative[Validated[E, _]]
    with {
      override def unit[A](a: => A): Validated[E, A] = Valid(a)

      extension [A](kore: Validated[E, A])
        override def map2[B, C](
            sore: Validated[E, B],
        )(f: (A, B) => C): Validated[E, C] =
          (kore, sore) match {
            case (Valid(k), Valid(s)) => Valid(f(k, s))
            case (Invalid(ko), Invalid(so)) =>
              Invalid(summon[Semigroup[E]].combine(ko, so))
            case (e @ Invalid(_), _) => e
            case (_, e @ Invalid(_)) => e
          }
    }
  }

  object ZipList {
    def apply[A](as: A*): ZipList[A] = LazyList.from(as)

    extension [A](z: ZipList[A]) {
      def toLazyList: LazyList[A] = z

    }

    given zipListApplicative: Applicative[ZipList] with {
      override def unit[A](a: => A): ZipList[A] = LazyList.continually(a)

      extension [A](kore: ZipList[A])
        override def map2[B, C](sore: ZipList[B])(f: (A, B) => C): ZipList[C] =
          kore.zip(sore).map(f.tupled)
    }
  }

  given eitherMonad[E]: Monad[Either[E, _]] with {
    override def unit[A](a: => A): Either[E, A] = Right(a)

    extension [A](kore: Either[E, A]) {
      override def flatMap[B](f: A => Either[E, B]): Either[E, B] =
        kore match
          case Left(value) => Left(value)
          case Right(value) => f(value)
    }

  }
}

trait Applicative[F[_]] extends Functor[F] {
  self =>
  // primitive combinators
  def unit[A](a: => A): F[A]

  def product[G[_]](G: Applicative[G]): Applicative[[x] =>> (F[x], G[x])] = new:
    def unit[A](a: => A): (F[A], G[A]) =
      (self.unit(a), G.unit(a))

    override def apply[A, B](fs: (F[A => B], G[A => B]))(
        p: (F[A], G[A]),
    ): (F[B], G[B]) =
      (self.apply(fs(0))(p(0)), G.apply(fs(1))(p(1)))

  extension [A](kore: F[A]) {
    // `map2` is implemented by first currying `f` so we get a function
    // of type `A => B => C`. This is a function that takes `A` and returns
    // another function of type `B => C`. We could map `f.curried` over
    // `F[A]` but let's stick with just `apply` and `unit`. We can lift
    // `f.curried` in to `F` via `unit`, giving us `F[A => B => C]`. Then
    // we can use `apply` along with `F[A]` to get `F[B => C]`. Passing
    // that to `apply` along with the `F[B]` will give us the desired `F[C]`.
    def map2[B, C](sore: F[B])(f: (A, B) => C): F[C] =
      apply(apply(unit(f.curried))(kore))(sore)

    override def map[B](f: A => B): F[B] =
      kore.map2(unit(()))((a, _) => f(a))

    def product[B](fb: F[B]): F[(A, B)] =
      kore.map2(fb)((_, _))
  }

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B] =
    fab.map2(fa)((fn, a) => fn(a))

  def replicateM[A](n: Int, fa: F[A]): F[List[A]] =
    sequence(List.fill(n)(fa))

  def sequence[A](fas: List[F[A]]): F[List[A]] =
    traverse(fas)(identity)

  def traverse[A, B](as: List[A])(f: A => F[B]): F[List[B]] =
    as.foldRight(unit(List.empty[B]))((a, acc) => f(a).map2(acc)(_ :: _))

  def traverseMap[K, V](kfv: Map[K, F[V]]): F[Map[K, V]] =
    kfv.foldLeft(unit(Map.empty[K, V])) { case (acc, (k, fv)) =>
      acc.map2(fv)((m, v) => m + (k -> v))
    }
}
