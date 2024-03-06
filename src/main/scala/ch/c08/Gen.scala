package ge.zgharbi.study.fps
package ch.c08

import ch.c06.{RNG, State}
import ch.c06.RNG.SimpleRNG
import ch.c08.Prop.*
import ch.c08.Prop.Result.{Falsified, Passed, Proved}

import scala.annotation.targetName
import scala.compiletime.{codeOf, error}

opaque type Prop = (MaxSize, TestCases, RNG) => Result
object Prop {

  opaque type SuccessCount <: Int = Int
  opaque type FailedCase = String
  opaque type TestCases <: Int = Int
  opaque type MaxSize <: Int = Int

  @targetName("forAllSized")
  def forAll[A](g: SGen[A])(f: A => Boolean): Prop =
    (max, n, rng) =>
      val casesPerSize = (n - 1) / max.toInt + 1
      val props: LazyList[Prop] =
        LazyList
          .from(0)
          .take((n min max) + 1)
          .map(i => forAll(g(i))(f))
      val prop: Prop =
        props
          .map[Prop](p => (max, n, rng) => p(max, casesPerSize, rng))
          .toList
          .reduce(_ && _)
      prop(max, n, rng)

  def forAll[A](as: Gen[A])(f: A => Boolean): Prop = (ms, n, rng) =>
    println(s"$ms, $n")
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

  extension (self: Prop) {
    def check(
        maxSize: MaxSize = 100,
        testCases: TestCases = 100,
        rng: RNG = SimpleRNG(System.currentTimeMillis),
    ): Result =
      self(maxSize, testCases, rng)

    def run(
        maxSize: MaxSize = 100,
        testCases: TestCases = 100,
        rng: RNG = SimpleRNG(System.currentTimeMillis),
    ): Unit =
      self(maxSize, testCases, rng) match
        case Passed => println(s"+ OK, Passed")
        case Falsified(msg, n) =>
          println(s"! Falsified after $n: $msg")
        case Proved => println(s"+ OK, Proved")

    @targetName("or")
    infix def ||(that: Prop): Prop = (ms, n, rng) =>
      self.tag("or-left")(ms, n, rng) match {
        case Falsified(msg, _) =>
          that.tag("or-right").tag(msg.string)(ms, n, rng)
        case x => x
      }

    @targetName("andAnd")
    def &&(that: Prop): Prop = (max, n, rng) =>
      self.tag("and-left")(max, n, rng) match
        case Passed | Proved => that.tag("and-right")(max, n, rng)
        case x => x

    def tag(msg: String): Prop =
      (max, n, rng) =>
        self(max, n, rng) match
          case Falsified(e, c) =>
            Falsified(FailedCase.fromString(s"$msg($e)"), c)
          case x => x
  }

  enum Result {
    case Passed
    case Falsified(failure: FailedCase, success: SuccessCount)
    case Proved

    def isFalsified: Boolean = this match
      case Passed => false
      case Falsified(_, _) => true
      case Proved => false
  }

  object SuccessCount:
    extension (sc: SuccessCount) def toInt: Int = sc
    def fromInt(s: Int): SuccessCount = s

  object FailedCase:
    extension (f: FailedCase) def string: String = f
    def fromString(s: String): FailedCase = s

  object TestCases:
    extension (tc: TestCases) def toInt: Int = tc
    def fromInt(s: Int): TestCases = s

  object MaxSize:
    extension (ms: MaxSize) def toInt: Int = ms
    def fromInt(s: Int): MaxSize = s
}

opaque type Gen[+A] = State[RNG, A]
object Gen {

  extension [A](self: Gen[A]) {
    def nonEmptyList: SGen[List[A]] = n => self.listOfN(n max 1)

    def unSized: SGen[A] = _ => self

    def list: SGen[List[A]] = n => self.listOfN(n)

    def listOfN(size: Int): Gen[List[A]] =
      Gen.listOfN(size, self)

    def listOfN(size: Gen[Int]): Gen[List[A]] =
      size.flatMap(listOfN)

    def flatMap[B](f: A => Gen[B]): Gen[B] = State.flatMap(self)(f)

    def map[B](f: A => B): Gen[B] = State.map(self)(f)
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
object SGen {
  extension [A](self: SGen[A]) {
    def map[B](f: A => B): SGen[B] = n => self(n).map(f)

    def flatMap[B](f: A => SGen[B]): SGen[B] = n =>
      self(n).flatMap(a => f(a)(n))
  }
}
