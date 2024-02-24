package ge.zgharbi.study.fps
package ch.c08

import ch.c06.{RNG, State}
import ch.c06.RNG.SimpleRNG
import ch.c08.Prop.{Result, TestCases}
import ch.c08.Prop.Result.{Falsified, Passed}

import scala.annotation.targetName

opaque type SuccessCount <: Int = Int
opaque type FailedCase <: String = String
object FailedCase {
  extension (f: FailedCase) def string: String = f
  def fromString(s: String): FailedCase = s
}
opaque type Prop = (TestCases, RNG) => Result
object Prop {
  extension (self: Prop) {
    def run(): Unit = self(100, RNG.SimpleRNG(System.currentTimeMillis)) match
      case Passed => println(s"+ OK, Passed")
      case Result.Falsified(msg, n) =>
        println(s"! Falsified after $n: $msg")

    @targetName("or")
    infix def ||(that: Prop): Prop = (n, rng) =>
      self.tag("or-left")(n, rng) match {
        case Falsified(msg, _) => that.tag("or-right").tag(msg.string)(n, rng)
        case x => x
      }

    @targetName("and")
    infix def &&(that: Prop): Prop = (n, rng) =>
      self.tag("and-left")(n, rng) match {
        case Passed => that.tag("and-right")(n, rng)
        case x => x
      }

    def tag(t: String): Prop = (n, rng) =>
      self(n, rng) match
        case Falsified(e, c) =>
          Falsified(FailedCase.fromString(s"$t($e)"), c)
        case x => x
  }
  opaque type TestCases = Int

  def forAll[A](as: Gen[A])(f: A => Boolean): Prop = (n, rng) =>
    randomLazyList(as)(rng)
      .zip(LazyList.from(0))
      .take(n)
      .map { case (a, i) =>
        try {
          if f(a) then Passed
          else Falsified(a.toString, i)
        } catch {
          case e: Exception => Falsified(buildMsg(a, e), i)
        }
      }
      .find(_.isFalsified)
      .getOrElse(Passed)

  def randomLazyList[A](g: Gen[A])(rng: RNG): LazyList[A] =
    LazyList.unfold(rng)(rng => Some(g.run(rng)))

  def buildMsg[A](a: A, e: Exception): String =
    s"test case: $a\n" +
      s"generated an exception: ${e.getMessage}\n" +
      s"stack trace:\n ${e.getStackTrace.mkString("\n")}"

  enum Result {
    case Passed
    case Falsified(failure: FailedCase, success: SuccessCount)

    def isFalsified: Boolean = this match
      case Result.Passed => false
      case Result.Falsified(_, _) => true
  }

  object TestCases {
    extension (x: TestCases) def toInt: Int = x
    def fromInt(x: Int): TestCases = x
  }
}

opaque type Gen[+A] = State[RNG, A]
object Gen {

  extension [A](self: Gen[A]) {
    def unsized: SGen[A] = _ => self

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

  def forAll[A](gen: Gen[A])(f: A => Boolean): Prop = ???
}

// Sized generator
opaque type SGen[+A] = Int => Gen[A]
