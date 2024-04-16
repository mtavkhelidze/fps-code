package ge.zgharbi.study.fps
package ex

import ch.c07Parallelism.NonBlocking.Par
import ch.c08Testing.exhaustive.Gen.**
import ch.c10MonoidFoldable.{Monoid, WC}
import common.Common.*
import common.PropSuite

import java.util.concurrent.{Executors, ExecutorService}

class E10MonoidSuite extends PropSuite {
  import Monoid.*

  private lazy val service: ExecutorService = Executors.newFixedThreadPool(4)

  override def afterAll(): Unit = {
    super.afterAll()
    service.shutdownNow()
  }

  import Monoid.given

  given Monoid[String] = stringMonoid

  given Monoid[Int] = intAddition


  test("Monoid.mapMergeMonoid")(genUnit) { _ =>
    val M = mapMergeMonoid[String, Map[String, Int]]
    val m1 = Map("o1" -> Map("i1" -> 1, "i2" -> 2))
    val m2 = Map("o1" -> Map("i2" -> 3))
    val m3 = M.combine(m1, m2)
    assertEquals(m3, Map("o1" -> Map("i1" -> 1, "i2" -> 5)))
  }

  test("E10.16 Product Monoid laws")(genString ** genString ** genString ** genInt ** genInt ** genInt) {
    case s1 ** s2 ** s3 ** i1 ** i2 ** i3 =>
      import Monoid.productMonoid

      assertMonoid(productMonoid[String, Int], (s1, i1), (s2, i2), (s3, i3))
  }

  test("E10.04 Monoid laws")(genInt ** genInt ** genInt) { case i1 ** i2 ** i3 =>
    assertMonoid(intAddition, i1, i2, i3)
  }
  test("E10.07 foldLeft using intAddition")(genUnit) { _ =>
    given intMonoid: Monoid[Int] = intAddition

    val xs = IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    assertEquals(foldLeft(xs)(identity), 55)
  }
  test("E10.07 foldLeft using stringMonoid")(genUnit) { _ =>
    given monoid: Monoid[String] = stringMonoid
    def toString = (i: Int) => i.toString
    val xs = IndexedSeq(1, 2, 3, 4, 5)

    assertEquals(foldLeft(xs)(toString), "12345")
  }
  test("E10.08 parFoldMap using stringMonoid")(genUnit) { _ =>

    given monoid: Monoid[String] = stringMonoid

    def toString = (i: Int) => i.toString

    val xs = 1 to 5

    val actual: Par[String] = parFoldMap(xs)(toString)
    val expected = xs.mkString

    assertEquals(actual.run(service), expected)
  }
  test("E10.11 WordCount monoid laws")(WC.wcGen ** WC.wcGen ** WC.wcGen) { case g1 ** g2 ** g3 =>
    assertMonoid(WC.monoid, g1, g2, g3)
  }

  test("E10.12 count words")(genUnit) { _ =>
    val input = "This book is a treatise on the theory of ethics"
    val result = WC.count(input)
    assertEquals(result, 10)
  }

  private def trueCounter(b: Boolean): Int = if b then 1 else 0

  private def trueCounter(list: Seq[Boolean]): Int = list.count(identity)

  private def assertMonoid[A](m: Monoid[A], a: A, b: A, c: A): Unit =
    assertEquals(m.combine(a, m.combine(b, c)), m.combine(m.combine(a, b), c), "associativity")
    assertEquals(m.combine(a, m.empty), a, "identity")
    assertEquals(m.combine(m.empty, a), a, "identity")

}
