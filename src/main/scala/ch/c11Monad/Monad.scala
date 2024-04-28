package ge.zgharbi.study.fps
package ch.c11Monad

import ch.c08Testing.*

trait Monad[F[_]] extends Functor[F] {
  def unit[A](a: => A): F[A]

  def sequence[A](fas: List[F[A]]): F[List[A]] =
    fas.foldRight(unit(List.empty[A]))((fa, acc) => fa.map2(acc)(_ :: _))

  def traverse[A, B](as: List[A])(f: A => F[B]): F[List[B]] =
    as.foldRight(unit(List.empty[B]))((a, acc) => f(a).map2(acc)(_ :: _))

  def replicateM[A](n: Int, fa: F[A]): F[List[A]] =
    sequence(List.fill(n)(fa))

  def filterM[A](as: List[A])(f: A => F[Boolean]): F[List[A]] =
    as.foldRight(unit(List.empty[A])) { (a, acc) =>
      f(a).flatMap(b => if b then unit(a).map2(acc)(_ :: _) else acc)
    }

  extension [A](fa: F[A]) {
    def product[B](fb: F[B]): F[(A, B)] = fa.map2(fb)((_, _))

    def flatMap[B](f: A => F[B]): F[B]

    override def map[B](f: A => B): F[B] = flatMap(a => unit(f(a)))

    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] = {
      fa.flatMap(a => fb.map(b => f(a, b)))
    }
  }
}

object Monad {
  given genMonad: Monad[Gen] with {
    override def unit[A](a: => A): Gen[A] = Gen.unit(a)

    extension [A](fa: Gen[A])
      override def flatMap[B](f: A => Gen[B]): Gen[B] = Gen.flatMap(fa)(f)
  }

  given optionMonad: Monad[Option] with {

    override def unit[A](a: => A): Option[A] = Some(a)

    extension [A](fa: Option[A])
      override def flatMap[B](f: A => Option[B]): Option[B] = fa.flatMap(f)
  }
}

trait Functor[F[_]] {
  extension [A](fa: F[A]) {
    def map[B](f: A => B): F[B]
  }
  extension [A, B](fab: F[(A, B)]) {
    def distribute: (F[A], F[B]) = (fab.map(_(0)), fab.map(_(1)))
  }
  extension [A, B](e: Either[F[A], F[B]]) {
    def coDistribute: F[Either[A, B]] = e match
      case Left(fa) => fa.map(Left(_))
      case Right(fb) => fb.map(Right(_))
  }
}

object Functor {
  given listFunction: Functor[List] with {
    extension [A](as: List[A])
      override def map[B](f: A => B): List[B] = as.map(f)
  }
}
