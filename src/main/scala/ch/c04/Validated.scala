package ge.zgharbi.study.fps
package ch.c04

enum Validated[+E, +A] {
  case Valid(get: A)
  case Invalid(errors: E)

  def map2[EE >: E, B, C](
      b: Validated[EE, B],
  )(f: (A, B) => C)(using combine: (EE, EE) => EE): Validated[EE, C] =
    (this, b) match {
      case (Valid(aa), Valid(bb))     => Valid(f(aa, bb))
      case (Invalid(es), Valid(_))    => Invalid(es)
      case (Valid(_), Invalid(es))    => Invalid(es)
      case (Invalid(ae), Invalid(be)) => Invalid(combine(ae, be))
    }
}

object Validated {

  def valid[E, A](a: A): Validated[E, A] = Valid(a)
  def invalid[E, A](e: E): Validated[E, A] = Invalid(e)

  def sequence[E, A](vs: List[Validated[E, A]])(using
      (E, E) => E,
  ): Validated[E, List[A]] =
    traverse(vs)(identity)

  def traverse[E, A, B](as: List[A])(
      f: A => Validated[E, B],
  )(using combineErr: (E, E) => E): Validated[E, List[B]] =
    as.foldRight(Valid(Nil): Validated[E, List[B]])((a, acc) =>
      f(a).map2(acc)(_ :: _),
    )

  def fromEither[E, A](e: Either[E, A]): Validated[E, A] =
    e match
      case Either.Left(es) => Invalid(es)
      case Either.Right(a) => Valid(a)

  extension [E, A](v: Validated[E, A])
    def toEither: Either[E, A] = v match {
      case Valid(a)   => Either.Right(a)
      case Invalid(e) => Either.Left(e)
    }

    def map[B](f: A => B): Validated[E, B] = v match {
      case Valid(a)   => Valid(f(a))
      case Invalid(e) => Invalid(e)
    }

    def flatMap[B](f: A => Validated[E, B]): Validated[E, B] = v match {
      case Valid(a)   => f(a)
      case Invalid(e) => Invalid(e)
    }
}
