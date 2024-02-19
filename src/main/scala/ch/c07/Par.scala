package ge.zgharbi.study.fps
package ch.c07

type Par[A]

object Examples {
  import Par.*
  def sum(ints: IndexedSeq[Int]): Par[Int] =
    if ints.size <= 0 then Par.unit(ints.headOption.getOrElse(0))
    else
      val (l, r) = ints.splitAt(ints.size / 2)
      Par.fork(sum(l)).map2(Par.fork(sum(r)))(_ + _)
}
object Par {
  def lazyUnit[A](a: => A): Par[A] = fork(unit(a))

  def unit[A](a: A): Par[A] = ???

  def fork[A](a: => Par[A]): Par[A] = ???

  extension [A](pa: Par[A])
    def run: A = ???
    def map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C] = ???
}
