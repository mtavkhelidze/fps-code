package ge.zgharbi.study.fps
package ex

import ch.c07Parallelism.NonBlocking.Par
import ch.c08Testing.{Gen, Prop}
import ch.c10.{Monoid, WC}
import common.Common.*

import munit.FunSuite

class E10MonoidSuite extends FunSuite {
  import Monoid.*

  override def afterAll(): Unit = {
    super.afterAll()
    service.shutdownNow()
  }

  test("E10.04 Monoid laws") {
    assertEquals(
      monoidLaws(Monoid.intAddition, Gen.int).check(),
      Prop.Result.Passed
    )
  }
  test("E10.07 foldLeft using intAddition") {
    given intMonoid: Monoid[Int] = intAddition

    val xs = IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    assertEquals(foldLeft(xs)(identity), 55)
  }
  test("E10.07 foldLeft using stringMonoid") {
    given monoid: Monoid[String] = stringMonoid
    def toString = (i: Int) => i.toString
    val xs = IndexedSeq(1, 2, 3, 4, 5)

    assertEquals(foldLeft(xs)(toString), "12345")
  }
  test("E10.08 parFoldMap using stringMonoid") {

    given monoid: Monoid[String] = stringMonoid

    def toString = (i: Int) => i.toString

    val xs = 1 to 5

    val actual: Par[String] = parFoldMap(xs)(toString)
    val expected = xs.mkString

    assertEquals(actual.run(service), expected)
  }
  test("E10.11 WordCount monoid laws") {
    val checkResult = monoidLaws(WC.monoid, WC.wcGen).check()
    assertEquals(checkResult, Prop.Result.Passed)
  }

  private def trueCounter(b: Boolean): Int = if b then 1 else 0

  private def trueCounter(list: Seq[Boolean]): Int = list.count(identity)

  private def assertMonoid[A](m: Monoid[A], a: A, b: A, c: A): Unit =
    assertEquals(m.combine(a, m.combine(b, c)), m.combine(m.combine(a, b), c), "associativity")
    assertEquals(m.combine(a, m.empty), a, "identity")
    assertEquals(m.combine(m.empty, a), a, "identity")

}
