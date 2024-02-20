package ge.zgharbi.study.fps
package ch.c07

import java.util.concurrent.{Future as JavaFuture, *}

opaque type Par[A] = ExecutorService => JavaFuture[A]

object Par {

  def equal[A](e: ExecutorService)(p: Par[A], p2: Par[A]): Boolean =
    p(e).get == p2(e).get

  def parMap[A, B](ps: List[A])(f: A => B): Par[List[B]] =
    fork {
      val fbs: List[Par[B]] = ps.map(asyncF(f))
      sequence(fbs)
    }

  def parFilter[A](as: List[A])(f: A => Boolean): Par[List[A]] =
    fork {
      val pars: List[Par[List[A]]] =
        as.map(asyncF(a => if f(a) then List(a) else Nil))
      sequence(pars).map(_.flatten)
    }

  def sequenceBalanced[A](pas: IndexedSeq[Par[A]]): Par[IndexedSeq[A]] =
    if pas.isEmpty then unit(IndexedSeq.empty)
    else if pas.size == 1 then pas.head.map(IndexedSeq(_))
    else
      val (l, r) = pas.splitAt(pas.size / 2)
      sequenceBalanced(l).map2(sequenceBalanced(r))(_ ++ _)

  def sequence[A](ps: List[Par[A]]): Par[List[A]] =
    sequenceBalanced(ps.toIndexedSeq).map(_.toList)

  def asyncF[A, B](f: A => B): A => Par[B] =
    (a: A) => lazyUnit(f(a))

  def lazyUnit[A](a: => A): Par[A] = fork(unit(a))

  def unit[A](a: A): Par[A] = _ => UnitFuture(a)

  def delay[A](fa: => Par[A]): Par[A] = es => fa(es)
  
  def fork[A](fa: => Par[A]): Par[A] = es => es.submit(() => fa(es).get)

  private case class UnitFuture[A](get: A) extends JavaFuture[A] {
    override def cancel(mayInterruptIfRunning: Boolean): Boolean = false
    override def get(timeout: Long, unit: TimeUnit): A = get
    override def isCancelled: Boolean = false
    override def isDone: Boolean = true
  }

  extension [A](pa: Par[A])
    def run(s: ExecutorService): JavaFuture[A] = pa(s)

    def map[B](f: A => B): Par[B] =
      pa.map2(unit(()))((a, _) => f(a))

    def map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C] =
      (es: ExecutorService) =>
        val fa = pa(es)
        val fb = pb(es)
        UnitFuture(f(fa.get, fb.get))

    def map2Timeouts[B, C](pb: Par[B])(f: (A, B) => C): Par[C] =
      es =>
        new JavaFuture[C] {
          private val fa = pa(es)
          private val fb = pb(es)
          @volatile private var cache: Option[C] = None

          override def cancel(mayInterruptIfRunning: Boolean): Boolean =
            fa.cancel(mayInterruptIfRunning) || fb.cancel(mayInterruptIfRunning)

          override def isCancelled: Boolean = fa.isCancelled || fb.isCancelled

          override def isDone: Boolean = cache.isDefined

          override def get(): C = get(Long.MaxValue, TimeUnit.MILLISECONDS)

          override def get(timeout: Long, unit: TimeUnit): C = {
            val timeoutNs = TimeUnit.NANOSECONDS.convert(timeout, unit)
            val started = System.nanoTime
            val a = fa.get(timeoutNs, TimeUnit.NANOSECONDS)
            val elapsed = System.nanoTime - started
            val b = fb.get(timeoutNs - elapsed, TimeUnit.NANOSECONDS)
            val c = f(a, b)
            cache = Some(c)
            c
          }
        }
}

object Examples {
  import Par.*

  // sort the list on the left and do nothing on the right
  def sortPar(parList: Par[List[Int]]): Par[List[Int]] =
    parList.map(_.sorted)

  def sum(ints: IndexedSeq[Int]): Par[Int] =
    if ints.size <= 0 then Par.unit(ints.headOption.getOrElse(0))
    else
      val (l, r) = ints.splitAt(ints.size / 2)
      Par.fork(sum(l)).map2(Par.fork(sum(r)))(_ + _)
}

@main def main(): Unit = {
  import ch.c07.Par.*

  val a = lazyUnit(42 + 1)
  val es = Executors.newFixedThreadPool(2)
  println(Par.equal(es)(a, fork(a)))
}
