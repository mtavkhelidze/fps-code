package ge.zgharbi.study.fps
package ch.c07

import java.util.concurrent.{Callable, CountDownLatch, ExecutorService}
import java.util.concurrent.atomic.AtomicReference

object NonBlocking {
  opaque type Future[A] = (A => Unit) => Unit
  opaque type Par[A] = ExecutorService => Future[A]

  object Par {
    def choiceMap[K, V](key: Par[K])(choices: Map[K, Par[V]]): Par[V] =
      es => cb => key(es)(k => choices(k)(es)(cb))

    def choiceN[A](p: Par[Int])(choices: List[Par[A]]): Par[A] =
      es =>
        cb =>
          p(es) { n =>
            val index = n % choices.size
            eval(es)(choices(index)(es)(cb))
          }
    def choice[A](cond: Par[Boolean])(t: Par[A], f: Par[A]): Par[A] =
      es =>
        cb =>
          cond(es) { b =>
            if b then eval(es)(t(es)(cb))
            else eval(es)(f(es)(cb))
          }

    private def eval(es: ExecutorService)(r: => Unit): Unit =
      es.submit(new Callable[Unit] {
        override def call: Unit = r
      })

    def unit[A](a: A): Par[A] = _ => cb => cb(a)

    def fork[A](a: => Par[A]): Par[A] =
      es => cb => eval(es)(a(es)(cb))

    extension [A](pa: Par[A])
      def map[B](f: A => B): Par[B] =
        pa.map2(unit(()))((a, _) => f(a))

      def map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C] =
        es =>
          cb =>
            var ar: Option[A] = None
            var br: Option[B] = None
            val combiner = Actor[Either[A, B]](es) {
              case Left(a) =>
                if br.isDefined then eval(es)(cb(f(a, br.get)))
                else ar = Some(a)
              case Right(b) =>
                if ar.isDefined then eval(es)(cb(f(ar.get, b)))
                else br = Some(b)
            }
            pa(es)(a => combiner ! Left(a))
            pb(es)(b => combiner ! Right(b))

      def run(es: ExecutorService): A =
        val ref = new AtomicReference[A]
        val latch = new CountDownLatch(1)
        pa(es) { a =>
          ref.set(a)
          latch.countDown()
        }
        latch.await()
        ref.get
  }
}
