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

  def listOfN[A](n: Int, gen: Gen[A]): Gen[List[A]] =
    State.sequence(List.fill(n)(gen))

  def unit[A](a: => A): Gen[A] = State.unit(a)

  def boolean: Gen[Boolean] = State(RNG.boolean)

  def choose(start: Int, stopExclusive: Int): Gen[Int] =
    State(RNG.nonNegativeInt).map(n => start + n % (stopExclusive - start))

//  def pair(start: Int, stopExclusive: Int): Gen[(Int, Int)] =

  def forAll[A](gen: Gen[A])(f: A => Boolean): Prop = ???
}
