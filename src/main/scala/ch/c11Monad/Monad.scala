package ge.zgharbi.study.fps
package ch.c11Monad

import ch.c06State.State
import ch.c08Testing.*

import ge.zgharbi.study.fps.ch.c12Applicative.Applicative


opaque type Reader[-R, +A] = R => A

object Reader {
  def ask[R]: Reader[R, R] = r => r

  def apply[R, A](f: R => A): Reader[R, A] = f

  extension [R, A](ra: Reader[R, A]) {
    def run(r: R): A = ra(r)
  }

  given readerMonad[R]: Monad[[x] =>> Reader[R, x]] with {
    override def unit[A](a: => A): Reader[R, A] = _ => a

    extension [A](fa: Reader[R, A])
      override def flatMap[B](f: A => Reader[R, B]): Reader[R, B] =
        r => f(fa(r))(r)
  }
}

case class Id[A](a: A) {
  def map[B](f: A => B): Id[B] = Id(f(a))

  def flatMap[B](f: A => Id[B]): Id[B] = f(a)
}

object Id {
  given idMonad: Monad[Id] with {

    override def unit[A](a: => A): Id[A] = Id(a)

    extension [A](fa: Id[A])
      override def flatMap[B](f: A => Id[B]): Id[B] = fa.flatMap(f)
  }
}

trait Monad[F[_]] extends Applicative[F] {
  def join[A](ffa: F[F[A]]): F[A] =
    ffa.flatMap(identity)

  def compose[A, B, C](f: A => F[B], g: B => F[C]): A => F[C] =
    a => f(a).flatMap(g)

  def unit[A](a: => A): F[A]

  def filterM[A](as: List[A])(f: A => F[Boolean]): F[List[A]] =
    as.foldRight(unit(List.empty[A])) { (a, acc) =>
      f(a).flatMap(b => if b then unit(a).map2(acc)(_ :: _) else acc)
    }

  extension [A](fa: F[A]) {
    def flatMapViaCompose[B](fb: A => F[B]): F[B] =
      compose(_ => fa, fb)(())


    def flatMap[B](f: A => F[B]): F[B]

    override def map[B](f: A => B): F[B] = flatMap(a => unit(f(a)))

    override def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] = {
      fa.flatMap(a => fb.map(b => f(a, b)))
    }
  }
}

object Monad {
  given stateMonad[S]: Monad[[x] =>> State[S, x]] with {
    override def unit[A](a: => A): State[S, A] = State(s => (a, s))

    extension [A](fa: State[S, A])
      override def flatMap[B](f: A => State[S, B]): State[S, B] = State.flatMap(fa)(f)
  }

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
