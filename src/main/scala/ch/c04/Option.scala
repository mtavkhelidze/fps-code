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
}
