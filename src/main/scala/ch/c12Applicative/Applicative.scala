package ge.zgharbi.study.fps
package ch.c12Applicative

import ch.c11Monad.Functor

trait Applicative[F[_]] extends Functor[F] {
  // primitive combinators
  def unit[A](a: => A): F[A]
  extension [A](kore: F[A]) {
    def map2[B, C](sore: F[B])(f: (A, B) => C): F[C]
  }
}
