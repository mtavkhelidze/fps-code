package ge.zgharbi.study.fps
package ch.c04

enum Either[+E, +A] {
  case Left(value: E)
  case Right(value: A)

  def map[B](f: A => B): Either[E, B] = this match {
    case Left(e) => Left(e)
    case Right(a) => Right(f(a))
  }

  def orElse[EE >: E, B >: A](b: => Either[EE, B]): Either[EE, B] =
    this match {
      case Left(_) => b
      case Right(x) => Right(x)
    }

  def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B] =
    this match {
      case Left(e) => Left(e)
      case Right(a) => f(a)
    }

  def map2[EE >: E, B, C](
      that: Either[EE, B],
  )(f: (A, B) => C): Either[EE, C] = {
    for
      a <- this
      b <- that
    yield f(a, b)
  }
}

object Either {
  def sequence[E, A](as: List[Either[E, A]]): Either[E, List[A]] =
    traverse(as)(identity)

  def traverse[E, A, B](as: List[A])(f: A => Either[E, B]): Either[E, List[B]] =
    as.foldRight[Either[E, List[B]]](Right(Nil))((a, b) => f(a).map2(b)(_ :: _))
}
