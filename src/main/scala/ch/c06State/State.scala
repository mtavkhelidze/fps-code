package ge.zgharbi.study.fps
package ch.c06State

opaque type State[S, +A] = S => (A, S)

object State {
  def apply[A, S](f: S => (A, S)): State[S, A] = f

  def traverse[S, A, B](as: List[A])(f: A => State[S, B]): State[S, List[B]] =
    as.foldRight(unit[S, List[B]](Nil))((a, acc) => f(a).map2(acc)(_ :: _))

  def unit[S, A](a: A): State[S, A] = s => (a, s)

  def sequence[S, A](actions: List[State[S, A]]): State[S, List[A]] =
    actions
      .foldRight(
        unit[S, List[A]](Nil),
      )((f, acc) => f.map2(acc)(_ :: _))

  def modify[S](f: S => S): State[S, Unit] =
    for {
      s <- get
      _ <- set(f(s))
    } yield ()

  def get[S]: State[S, S] = s => (s, s)

  def set[S](s: S): State[S, Unit] = _ => ((), s)

  extension [S, A](self: State[S, A])
    def run(s: S): (A, S) = self(s)

    def flatMap[B](f: A => State[S, B]): State[S, B] = s =>
      val (a, s1) = self(s)
      f(a)(s1)

    def map[B](f: A => B): State[S, B] =
      self.flatMap(a => unit(f(a)))

    def map2[B, C](other: State[S, B])(f: (A, B) => C): State[S, C] =
      for {
        a <- self
        b <- other
      } yield f(a, b)
}
