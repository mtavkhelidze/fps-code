package ge.zgharbi.study.fps
package ch.c08

import ch.c06.{RNG, State}
import ch.c06.RNG.SimpleRNG
import ch.c08.Prop.Result
import ch.c08.Prop.Result.{Falsified, Passed}

import scala.annotation.targetName

opaque type SuccessCount = Int

opaque type FailedCase = String
object FailedCase:
  extension (f: FailedCase) def string: String = f
  def fromString(s: String): FailedCase = s

opaque type MaxSize = Int
object MaxSize:
  extension (ms: MaxSize) def toInt: Int = ms
  def fromInt(n: Int): MaxSize = n

opaque type TestCases = Int
object TestCases:
  extension (tc: TestCases) def toInt: Int = tc
  def fromInt(n: Int): TestCases = n

opaque type Prop = (MaxSize, TestCases, RNG) => Result
object Prop {
  extension (self: Prop) {
    def run(): Unit =
      self(100, 100, RNG.SimpleRNG(System.currentTimeMillis)) match
        case Passed => println(s"+ OK, Passed")
        case Result.Falsified(msg, n) =>
          println(s"! Falsified after $n: $msg")

    @targetName("or")
    infix def ||(that: Prop): Prop = (ms, n, rng) =>
      self.tag("or-left")(ms, n, rng) match {
        case Falsified(msg, _) =>
          that.tag("or-right").tag(msg.string)(ms, n, rng)
        case x => x
      }

    @targetName("andAnd")
    infix def &&(that: Prop): Prop = (ms, n, rng) =>
      self.tag("and-left")(ms, n, rng) match {
        case Passed => that.tag("and-right")(ms, n, rng)
        case x => x
      }

    def tag(t: String): Prop = (ms, n, rng) =>
      self(ms, n, rng) match
        case Falsified(e, c) =>
          Falsified(FailedCase.fromString(s"$t($e)"), c)
        case x => x
  }

  @targetName("forAllSized")
  def forAll[A](sg: SGen[A])(f: A => Boolean): Prop =
    (ms, n, rng) =>
      val casesPerSize = (n - 1) / ms + 1
      val props: LazyList[Prop] = LazyList
        .from(0)
        .take((n min ms) + 1)
        .map(i => forAll(sg(i))(f))
      val prop: Prop =
        props
          .map[Prop](p => (ms, _, rng) => p(ms, casesPerSize, rng))
          .toList
          .reduce(_ && _)
      prop(ms, n, rng)

  def forAll[A](as: Gen[A])(f: A => Boolean): Prop = (ms, n, rng) =>
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
