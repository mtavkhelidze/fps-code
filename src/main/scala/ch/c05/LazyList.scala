package ge.zgharbi.study.fps
package ch.c05

enum LazyList[+A] {
  case Empty
  case Cons(h: () => A, t: () => LazyList[A])
}

object LazyList {
  def cons[A](hd: => A, tl: => LazyList[A]): LazyList[A] =
    lazy val head = hd
    lazy val tail = tl
    Cons(() => head, () => tail)

  def apply[A](as: A*): LazyList[A] =
    if as.isEmpty then empty
    else cons(as.head, apply(as.tail*))

  def empty[A]: LazyList[A] = LazyList.Empty

  extension [A](ll: LazyList[A])
    def headOption: Option[A] = ll match {
      case Empty      => None
      case Cons(h, _) => Some(h())
    }
    def toList: List[A] = ll match
      case LazyList.Empty      => Nil
      case LazyList.Cons(h, t) => h() :: t().toList
}
