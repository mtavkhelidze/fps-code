package ge.zgharbi.study.fps
package ch.c08

import ch.c06.{RNG, State}

import scala.annotation.targetName

opaque type SuccessCount <: Int = Int
opaque type FailedCase <: String = String

trait Prop { self =>
  def check: Either[(FailedCase, SuccessCount), SuccessCount]
  @targetName("and")
  def &&(that: Prop): Prop = new Prop:
    override def check: Either[(FailedCase, SuccessCount), SuccessCount] = ???
}

opaque type Gen[+A] = State[RNG, A]

object Gen {

  extension [A](self: Gen[A]) {
    def listOfN(size: Int): Gen[List[A]] =
      Gen.listOfN(size, self)

    def listOfN(size: Gen[Int]): Gen[List[A]] =
      size.flatMap(listOfN)

    def flatMap[B](f: A => Gen[B]): Gen[B] = State.flatMap(self)(f)
  }

  def weighted[A](g1: (Gen[A], Double), g2: (Gen[A], Double)): Gen[A] =
    val th = g1(1).abs / (g1(1).abs + g2(1).abs)
    State(RNG.double).flatMap(d => if d < th then g1(0) else g2(0))

  def union[A](g1: Gen[A], g2: Gen[A]): Gen[A] =
    boolean.flatMap(b => if b then g1 else g2)

  def boolean: Gen[Boolean] = State(RNG.boolean)

  def listOfN[A](n: Int, gen: Gen[A]): Gen[List[A]] =
    State.sequence(List.fill(n)(gen))

  def unit[A](a: => A): Gen[A] = State.unit(a)

  def choose(start: Int, stopExclusive: Int): Gen[Int] =
    State(RNG.nonNegativeInt).map(n => start + n % (stopExclusive - start))

//  def pair(start: Int, stopExclusive: Int): Gen[(Int, Int)] =

  def forAll[A](gen: Gen[A])(f: A => Boolean): Prop = ???
}
