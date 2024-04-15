package ge.zgharbi.study.fps
package ch.c10MonoidFoldable

trait Foldable[F[_]] {
  extension [A](as: F[A]) {
    def foldRight[B](acc: B)(f: (A, B) => B): B
    def foldLeft[B](acc: B)(f: (B, A) => B): B
    def foldMap[B](f: A => B)(using m: Monoid[B]): B =
      as.foldRight(m.empty)((a, b) => m.combine(f(a), b))
    def combineAll(using m: Monoid[A]): A =
      as.foldLeft(m.empty)(m.combine)
  }
}

object Foldable {
  given Foldable[List] with {
    extension [A](as: List[A])
      override def foldRight[B](acc: B)(f: (A, B) => B): B =
        as.foldRight(acc)(f)
      override def foldLeft[B](acc: B)(f: (B, A) => B): B =
        as.foldLeft(acc)(f)
  }

  given Foldable[IndexedSeq] with {
    extension [A](as: IndexedSeq[A])
      override def foldRight[B](acc: B)(f: (A, B) => B): B =
        as.foldRight(acc)(f)
      override def foldLeft[B](acc: B)(f: (B, A) => B): B = ???
  }
}
