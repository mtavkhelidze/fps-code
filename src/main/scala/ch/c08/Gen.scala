package ge.zgharbi.study.fps
package ch.c08
import ch.c08.Prop.SuccessCount

import scala.annotation.targetName

type Gen[+A]

opaque type SuccessCount <: Int = Int
opaque type FailedCase <: String = String

trait Prop { self =>
  def check: Either[(FailedCase, SuccessCount), SuccessCount]
  @targetName("and")
  def &&(that: Prop): Prop = new Prop:
    override def check: Boolean = self.check && that.check
}

def listOf[A](gen: Gen[A]): Gen[List[A]] = listOfN(1, gen)

def listOfN[A](n: Int, gen: Gen[A]): Gen[List[A]] = ???

def forAll[A](gen: Gen[A])(f: A => Boolean): Prop = ???
