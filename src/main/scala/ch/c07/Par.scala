package ge.zgharbi.study.fps
package ch.c07

import java.util.concurrent.{Callable, ExecutorService, TimeUnit, Future as JavaFuture}

opaque type Par[A] = ExecutorService => JavaFuture[A]

object Par {
  def lazyUnit[A](a: => A): Par[A] = fork(unit(a))

  def unit[A](a: A): Par[A] = _ => UnitFuture(a)

  def fork[A](a: => Par[A]): Par[A] = es => es.submit(() => a(es).get)

  private case class UnitFuture[A](get: A) extends JavaFuture[A] {
    override def cancel(mayInterruptIfRunning: Boolean): Boolean = false
    override def get(timeout: Long, unit: TimeUnit): A = get
    override def isCancelled: Boolean = false
    override def isDone: Boolean = true
  }

  extension [A](pa: Par[A])
    def run(s: ExecutorService): JavaFuture[A] = pa(s)

    def map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C] =
      (es: ExecutorService) =>
        val fa = pa(es)
        val fb = pb(es)
        UnitFuture(f(fa.get, fb.get))
}

object Examples {
  import Par.*
  def sum(ints: IndexedSeq[Int]): Par[Int] =
    if ints.size <= 0 then Par.unit(ints.headOption.getOrElse(0))
    else
      val (l, r) = ints.splitAt(ints.size / 2)
      Par.fork(sum(l)).map2(Par.fork(sum(r)))(_ + _)
}
