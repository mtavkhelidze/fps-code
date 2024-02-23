package ge.zgharbi.study.fps
package ch.c08

import scala.annotation.targetName

type Gen[+A]
trait Prop { self =>
  def check: Boolean
  @targetName("and")
  def &&(that: Prop): Prop = new Prop:
    override def check: Boolean = self.check && that.check
}

def listOf[A](gen: Gen[A]): Gen[List[A]] = listOfN(1, gen)

def listOfN[A](n: Int, gen: Gen[A]): Gen[List[A]] = ???

def forAll[A](gen: Gen[A])(f: A => Boolean): Prop = ???
