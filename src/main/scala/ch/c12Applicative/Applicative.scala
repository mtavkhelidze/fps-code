package ge.zgharbi.study.fps
package ch.c12Applicative

import ch.c10MonoidFoldable.Monoid
import ch.c11Monad.Functor

object Applicative {
  enum Validated[+E, +A] {
    case Valid(get: A) extends Validated[Nothing, A]
    case Invalid(error: E) extends Validated[E, Nothing]
  }

  given validatedApplicative[E: Monoid]: Applicative[Validated[E, _]] with {

    import Validated.*

    override def unit[A](a: => A): Validated[E, A] = Valid(a)

    extension [A](kore: Validated[E, A])
      override def map2[B, C](sore: Validated[E, B])(f: (A, B) => C): Validated[E, C] =
        (kore, sore) match {
          case (Valid(k), Valid(s)) => Valid(f(k, s))
          case (Invalid(ko), Invalid(so)) =>
            Invalid(summon[Monoid[E]].combine(ko, so))
          case (e@Invalid(_), _) => e
          case (_, e@Invalid(_)) => e
        }
  }
  opaque type ZipList[+A] = LazyList[A]

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
  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B] =
    fab.map2(fa)((fn, a) => fn(a))
  // primitive combinators
  def unit[A](a: => A): F[A]

  extension [A](kore: F[A]) {
    def map2[B, C](sore: F[B])(f: (A, B) => C): F[C]
    override def map[B](f: A => B): F[B] =
      kore.map2(unit(()))((a, _) => f(a))

    def product[B](fb: F[B]): F[(A, B)] =
      kore.map2(fb)((_, _))
  }

  def replicateM[A](n: Int, fa: F[A]): F[List[A]] =
    sequence(List.fill(n)(fa))

  def sequence[A](fas: List[F[A]]): F[List[A]] =
    traverse(fas)(identity)

  def traverse[A, B](as: List[A])(f: A => F[B]): F[List[B]] =
    as.foldRight(unit(List.empty[B]))((a, acc) => f(a).map2(acc)(_ :: _))

}
