package ge.zgharbi.study.fps
package ex

import ch.c08.{Gen, Prop}
import ch.c10.Monoid

import munit.FunSuite

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

  test("E10.07 foldLeft") {
    import E10.foldLeft
    import Monoid.*
    given intMonoid: Monoid[Int] = intAddition

    val xs = IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    assertEquals(foldLeft(xs)(identity), 55)
  }
}
