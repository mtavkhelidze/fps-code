package ge.zgharbi.study.fps
package ch.c08

import ch.c06.{RNG, State}

import scala.annotation.targetName

opaque type Gen[+A] = State[RNG, A]

opaque type SuccessCount <: Int = Int
opaque type FailedCase <: String = String

trait Prop { self =>
  def check: Either[(FailedCase, SuccessCount), SuccessCount]
  @targetName("and")
  def &&(that: Prop): Prop = new Prop:
    override def check: Either[(FailedCase, SuccessCount), SuccessCount] = ???
}

def choose(start: Int, stopExclusive: Int): Gen[Int] =
  State(RNG.nonNegativeInt).map(n => start + n % (stopExclusive - start))
  
def listOf[A](gen: Gen[A]): Gen[List[A]] = listOfN(1, gen)

def listOfN[A](n: Int, gen: Gen[A]): Gen[List[A]] = ???

def forAll[A](gen: Gen[A])(f: A => Boolean): Prop = ???
