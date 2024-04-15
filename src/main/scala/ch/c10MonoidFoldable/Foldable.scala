package ge.zgharbi.study.fps
package ch.c10MonoidFoldable

import ch.c03Tree.Tree
import ch.c05LazyList.LazyList

import scala.annotation.tailrec

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
    extension [A](xs: List[A])
      override def foldRight[B](acc: B)(f: (A, B) => B): B =
        xs.foldRight(acc)(f)
      override def foldLeft[B](acc: B)(f: (B, A) => B): B =
        xs.foldLeft(acc)(f)
  }

  given Foldable[IndexedSeq] with {
    extension [A](as: IndexedSeq[A])
      override def foldRight[B](acc: B)(f: (A, B) => B): B =
        as.foldRight(acc)(f)
      override def foldLeft[B](acc: B)(f: (B, A) => B): B =
        as.foldLeft(acc)(f)
  }

  given Foldable[LazyList] with {
    extension [A](as: LazyList[A])
      @tailrec
      override def foldRight[B](acc: B)(f: (A, B) => B): B =
        as.foldRight(acc)(f)
      @tailrec
      override def foldLeft[B](acc: B)(f: (B, A) => B): B =
        as.foldLeft(acc)(f)
  }

  given Foldable[Tree] with {
    /*
   * Notice that in `Foldable[Tree].foldMap`, we don't actually use the
   * `empty` from the `Monoid`. This is because there is no empty tree. This
   * suggests that there might be a class of types that are foldable with
   * something "smaller" than a monoid, consisting only of an associative
   * `combine`. That kind of object (a monoid without a `empty`) is called
   * a semigroup. `Tree` itself is not a monoid, but it is a semigroup.
   */

    import Tree.{Branch, Leaf}

    extension [A](as: Tree[A])
      override def foldRight[B](acc: B)(f: (A, B) => B): B = as match
        case Leaf(a) => f(a, acc)
        case Branch(l, r) => l.foldRight(r.foldRight(acc)(f))(f)
      override def foldLeft[B](acc: B)(f: (B, A) => B): B = as match
        case Leaf(a) => f(acc, a)
        case Branch(l, r) => r.foldLeft(l.foldLeft(acc)(f))(f)
      override def foldMap[B](f: A => B)(using mb: Monoid[B]): B = as match
        case Leaf(a) => f(a)
        case Branch(l, r) => mb.combine(l.foldMap(f), r.foldMap(f))
  }
}
