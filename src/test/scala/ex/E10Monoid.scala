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
          m.combine(a, m.zero) == a && m.combine(m.zero, a) == a
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
}
