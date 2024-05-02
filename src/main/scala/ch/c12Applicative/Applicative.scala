package ge.zgharbi.study.fps
package ch.c12Applicative

import ch.c11Monad.Functor

trait Applicative[F[_]] extends Functor[F] {
  // primitive combinators
  def unit[A](a: => A): F[A]
  extension [A](kore: F[A]) {
    def map2[B, C](sore: F[B])(f: (A, B) => C): F[C]
  }

  // derived combinators
  def traverse[A, B](as: List[A])(f: A => F[B]): F[List[B]] =
    as.foldRight(unit(List.empty[B]))((a, acc) => f(a).map2(acc)(_ :: _))

  extension [A](kore: F[A])
    override def map[B](f: A => B): F[B] =
      kore.map2(unit(()))((a, _) => f(a))
}
