package ge.zgharbi.study.fps
package ch.c08Testing

import ch.c06State.{RNG, State}
import ch.c06State.RNG.Simple
import ch.c08Testing.Prop.*
import ch.c08Testing.Prop.Result.{Falsified, Passed, Proved}

import scala.annotation.targetName
import scala.compiletime.{codeOf, error}

type Prop = (MaxSize, TestCases, RNG) => Result
object Prop {

  opaque type SuccessCount <: Int = Int
  opaque type FailedCase <: String = String
  opaque type TestCases <: Int = Int
  opaque type MaxSize <: Int = Int

  def verify(p: => Boolean): Prop =
    (_, _, _) => if p then Proved else Falsified("()", 0)

  @targetName("forAllSized")
  def forAll[A](g: SGen[A])(f: A => Boolean): Prop =
    (maxCases, nCases, rng) =>
      val casesPerSize = (nCases - 1) / maxCases + 1
      val props: LazyList[Prop] =
        LazyList
          .from(0)
          .take((nCases min maxCases) + 1)
          .map(i => forAll(g(i))(f))
      val prop: Prop =
        props
          .map[Prop](p => (max, _, rng) => p(max, casesPerSize, rng))
          .toList
          .reduce(_ && _)
      prop(maxCases, nCases, rng)

  def forAll[A](as: Gen[A])(f: A => Boolean): Prop = (_, n, rng) =>
    randomLazyList(as)(rng)
      .zip(LazyList.from(0))
      .take(n)
      .map { case (a, i) =>
        try if f(a) then Passed else Falsified(a.toString, i)
        catch case e: Exception => Falsified(buildMsg(a, e), i)
      }
      .find(_.isFalsified)
      .getOrElse(Passed)

  def buildMsg[A](a: A, e: Exception): String =
    s"test case: $a\n" +
      s"generated an exception: ${e.getMessage}\n" +
      s"stack trace:\n ${e.getStackTrace.mkString("\n")}"

  def randomLazyList[A](g: Gen[A])(rng: RNG): LazyList[A] =
    LazyList.unfold(rng)(rng => Some(g.run(rng)))

  extension (self: Prop) {
    def check(
               maxSize: MaxSize = 100,
               testCases: TestCases = 100,
               rng: RNG = Simple(System.currentTimeMillis),
    ): Result =
      self(maxSize, testCases, rng)

    def run(
             maxSize: MaxSize = 100,
             testCases: TestCases = 100,
             rng: RNG = Simple(System.currentTimeMillis),
    ): Unit =
      self.check(maxSize, testCases, rng) match
        case Passed => println(s"+ OK, Passed $testCases tests.")
        case Proved => println(s"+ OK, Proved property.")
        case Falsified(msg, n) =>
          println(s"! Falsified after $n passed tests:\n$msg.")

    @targetName("or")
    def ||(that: Prop): Prop = (ms, n, rng) =>
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
            val str = if e.toString.startsWith(msg) then e else s"$msg($e)"
            Falsified(FailedCase.fromString(str), c)
          case x => x
  }

  enum Result {
    case Falsified(failure: FailedCase, success: SuccessCount)
    case Passed
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

  @targetName("unProduct")
  object `**` {
    def unapply[A, B](p: (A, B)): Option[(A, B)] = Some(p)
  }

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

    def map2[B, C](that: Gen[B])(f: (A, B) => C): Gen[C] =
      State.map2(self)(that)(f)

    @targetName("product")
    def **[B](gb: Gen[B]): Gen[(A, B)] =
      map2(gb)((_, _))

  }

  def stringN(n: Int): Gen[String] =
    listOfN(n, choose(0, 127)).map(_.map(_.toChar).mkString)

  def weighted[A](g1: (Gen[A], Double), g2: (Gen[A], Double)): Gen[A] =
    val th = g1(1).abs / (g1(1).abs + g2(1).abs)
    State(RNG.double).flatMap(d => if d < th then g1(0) else g2(0))

  def union[A](g1: Gen[A], g2: Gen[A]): Gen[A] =
    boolean.flatMap(b => if b then g1 else g2)

  def boolean: Gen[Boolean] = State(RNG.boolean)

  def int: Gen[Int] = State(RNG.int)

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
