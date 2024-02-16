package ge.zgharbi.study.fps
package ch.c04

enum Validated[+E, +A] {
  case Valid(get: A)
  case Invalid(errors: List[E])
}

object Validated {
  def valid[E, A](a: A): Validated[E, A] = Valid(a)
  def invalid[E, A](e: E): Validated[E, A] = Invalid(List(e))

  extension [E, A](v: Validated[E, A])
    def map[B](f: A => B): Validated[E, B] = v match {
      case Valid(a)   => Valid(f(a))
      case Invalid(e) => Invalid(e)
    }

    def flatMap[B](f: A => Validated[E, B]): Validated[E, B] = v match {
      case Valid(a)   => f(a)
      case Invalid(e) => Invalid(e)
    }

    def map2[B, C](other: Validated[E, B])(f: (A, B) => C): Validated[E, C] = ???
}
