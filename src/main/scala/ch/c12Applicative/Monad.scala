package ge.zgharbi.study.fps
package ch.c12Applicative

/** A minimal implementation of `Monad` must implement `unit` and override
  * either `flatMap` or `join` and map.
  */
trait Monad[F[_]] extends Applicative[F] {
  extension [A](kore: F[A]) {
    def flatMap[B](f: A => F[B]): F[B] =
      kore.map(f).join
    override def map[B](f: A => B): F[B] =
      kore.flatMap(a => unit(f(a)))
    override def map2[B, C](sore: F[B])(f: (A, B) => C): F[C] =
      kore.flatMap(a => sore.map(b => f(a, b)))
  }

  extension [A](koreKore: F[F[A]]) {
    def join: F[A] = koreKore.flatMap(identity)
  }

  def compose[A, B, C](f: A => F[B], g: B => F[C]): A => F[C] =
    a => f(a).flatMap(g)
}
