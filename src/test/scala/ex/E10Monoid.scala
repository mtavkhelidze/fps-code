package ge.zgharbi.study.fps
package ex

import ch.c07Parallelism.NonBlocking.Par
import ch.c08.{Gen, Prop}
import ch.c10.Monoid
import ch.c10.Monoid.*

import munit.FunSuite

import java.util.concurrent.Executors

class E10Monoid extends FunSuite {
  import Monoid.*
  test("E10.04 Monoid laws") {
    def monoidLaws[A](m: Monoid[A], gen: Gen[A]): Prop = {
      val identity = Prop
        .forAll(gen) { a =>
          m.combine(a, m.empty) == a && m.combine(m.empty, a) == a
        }
        .tag("identity")

      val associativity = Prop
        .forAll(gen ** gen) { (x, y) =>
          m.combine(x, y) == m.combine(y, x)
        }
        .tag("associativity")

      identity && associativity
    }

    assertEquals(monoidLaws(Monoid.intAddition, Gen.int).check(), Prop.Result.Passed)
  }
  test("E10.07 foldLeft using intAddition") {
    import Monoid.*
    given intMonoid: Monoid[Int] = intAddition

    val xs = IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    assertEquals(foldLeft(xs)(identity), 55)
  }
  test("E10.07 foldLeft using stringMonoid") {
    import Monoid.*

    given monoid: Monoid[String] = stringMonoid
    def toString = (i: Int) => i.toString
    val xs = IndexedSeq(1, 2, 3, 4, 5)

    assertEquals(foldLeft(xs)(toString), "12345")
  }
  test("E10.08 parFoldMap using stringMonoid") {

    given monoid: Monoid[String] = stringMonoid

    def toString = (i: Int) => i.toString

    val es = Executors.newScheduledThreadPool(4)
    val xs = 1 to 5

    val actual: Par[String] = parFoldMap(xs)(toString)
    val expected = Par.unit(xs.mkString)

    assertEquals(actual.run(es), expected.run(es))
  }
}
