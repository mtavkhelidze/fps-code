package ge.zgharbi.study.fps
package ch.c10MonoidFoldable

trait Foldable[F[_]] {
  extension [A](as: F[A]) {
    def foldRight[B](acc: B)(f: (A, B) => B): B
    def foldLeft[B](acc: B)(f: (B, A) => B): B
    def foldMap[B](f: A => B)(using m: Monoid[B]): B
    def combineAll(using m: Monoid[A]): A =
      as.foldLeft(m.empty)(m.combine)
  }
}
