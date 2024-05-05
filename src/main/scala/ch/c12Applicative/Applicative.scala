package ge.zgharbi.study.fps
package ch.c12Applicative

import ch.c11Monad.Functor

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
