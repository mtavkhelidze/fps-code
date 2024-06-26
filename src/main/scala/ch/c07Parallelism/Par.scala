package ge.zgharbi.study.fps
package ch.c07Parallelism

import java.util.concurrent.{Future as JavaFuture, *}

opaque type Par[A] = ExecutorService => JavaFuture[A]

object Par {
  def choice[A](p: Par[Boolean])(t: Par[A], f: Par[A]): Par[A] =
    p.flatMap(b => if b then t else f)

  def choiceN[A](p: Par[Int])(choices: List[Par[A]]): Par[A] =
    p.flatMap(i => choices(i))

  def choiceMap[K, V](key: Par[K])(choices: Map[K, Par[V]]): Par[V] =
    es =>
      val k = key.run(es).get
      choices(k).run(es)

  def fork[A](a: => Par[A]): Par[A] =
    es =>
      es.submit(new Callable[A] {
        def call: A = a(es).get
      })

  def join[A](ppa: Par[Par[A]]): Par[A] =
    ppa.flatMap(identity)

  extension [A](pa: Par[A]) {

    def run(s: ExecutorService): JavaFuture[A] =
      pa(s)

    def flatMap[B](f: A => Par[B]): Par[B] =
      es =>
        val a = pa.run(es).get
        f(a).run(es)

    def map[B](f: A => B): Par[B] =
      pa.map2(unit(()))((a, _) => f(a))

    def map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C] =
      es =>
        val af = pa(es)
        val bf = pb(es)
        UnitFuture(f(af.get, bf.get))

    def chooser[B](f: A => Par[B]): Par[B] =
      es =>
        val a = pa(es).get
        f(a).run(es)

    def map2Timeouts[B, C](pb: Par[B])(f: (A, B) => C): Par[C] =
      es =>
        new JavaFuture[C]:
          private val futureA = pa(es)
          private val futureB = pb(es)
          @volatile private var cache: Option[C] = None

          def isDone: Boolean = cache.isDefined

          def get(): C = get(Long.MaxValue, TimeUnit.NANOSECONDS)

          def get(timeout: Long, units: TimeUnit): C =
            val timeoutNanos = TimeUnit.NANOSECONDS.convert(timeout, units)
            val started = System.nanoTime
            val a = futureA.get(timeoutNanos, TimeUnit.NANOSECONDS)
            val elapsed = System.nanoTime - started
            val b = futureB.get(timeoutNanos - elapsed, TimeUnit.NANOSECONDS)
            val c = f(a, b)
            cache = Some(c)
            c

          def isCancelled: Boolean =
            futureA.isCancelled || futureB.isCancelled

          def cancel(evenIfRunning: Boolean): Boolean =
            futureA.cancel(evenIfRunning) || futureB.cancel(evenIfRunning)
  }

  def equal[A](e: ExecutorService)(p: Par[A], p2: Par[A]): Boolean =
    p(e).get == p2(e).get

  def parMap[A, B](ps: List[A])(f: A => B): Par[List[B]] =
    fork {
      val fbs: List[Par[B]] = ps.map(asyncF(f))
      sequence(fbs)
    }

  def parMap[A, B](ps: IndexedSeq[A])(f: A => B): Par[IndexedSeq[B]] =
    fork {
      val fbs: IndexedSeq[Par[B]] = ps.map(asyncF(f))
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

  def sequence[A](as: IndexedSeq[Par[A]]): Par[IndexedSeq[A]] =
    sequenceBalanced(as)

  def asyncF[A, B](f: A => B): A => Par[B] =
    (a: A) => lazyUnit(f(a))

  def lazyUnit[A](a: => A): Par[A] = fork(unit(a))

  def unit[A](a: A): Par[A] = _ => UnitFuture(a)

  def delay[A](fa: => Par[A]): Par[A] = es => fa(es)

  private case class UnitFuture[A](get: A) extends JavaFuture[A]:
    def isDone = true

    def get(timeout: Long, units: TimeUnit): A = get

    def isCancelled = false

    def cancel(evenIfRunning: Boolean): Boolean = false
}
