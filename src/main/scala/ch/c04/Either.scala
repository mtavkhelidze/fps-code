package ge.zgharbi.study.fps
package ch.c04

enum Either[+E, +A] {
  case Left(value: E)
  case Right(value: A)

  def map[B](f: A => B): Either[E, B] = this match {
    case Left(e)  => Left(e)
    case Right(a) => Right(f(a))
  }

  def orElse[EE >: E, B >: A](b: => Either[EE, B]): Either[EE, B] =
    this match {
      case Left(_)  => b
      case Right(x) => Right(x)
    }

  def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B] =
    this match {
      case Left(e)  => Left(e)
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
