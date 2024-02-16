package ge.zgharbi.study.fps
package ch.c04

enum Option[+A] {
  case Some(get: A)
  case None

  def orElse[B >: A](ob: => Option[B]): Option[B] =
    map(Some(_)).getOrElse(ob)

  def filter(f: A => Boolean): Option[A] =
    flatMap(x => if f(x) then Some(x) else None)

  def flatMap[B](f: A => Option[B]): Option[B] =
    map(f).getOrElse(None)

  def map[B](f: A => B): Option[B] = this match {
    case Some(get) => Some(f(get))
    case None      => None
  }

  def getOrElse[B >: A](default: => B): B = this match {
    case Some(get) => get
    case None      => default
  }
}

object Option {
  def apply[A](a: A): Option[A] = Some(a)

  def lift[A, B](f: A => B): Option[A] => Option[B] = _ map f

  // using traverse
  def sequence[A](as: List[Option[A]]): Option[List[A]] =
    traverse(as)(identity)

  def traverse[A, B](as: List[A])(f: A => Option[B]): Option[List[B]] =
    as.foldRight(Some(Nil): Option[List[B]])((a, acc) =>
      map2(f(a), acc)(_ :: _),
    )

  def map2[A, B, C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] =
    a.flatMap(x => b.map(y => f(x, y)))
}
