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
}
