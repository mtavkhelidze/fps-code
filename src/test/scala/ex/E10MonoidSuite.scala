package ge.zgharbi.study.fps
package ex

import ch.c07Parallelism.NonBlocking.Par
import ch.c08Testing.exhaustive.Gen.**
import ch.c10MonoidFoldable.{Monoid, WC}
import ch.c10MonoidFoldable.Monoid.{*, given}
import common.Common.*
import common.PropSuite

import java.util.concurrent.{Executors, ExecutorService}
class E10MonoidSuite extends PropSuite {

  private lazy val service: ExecutorService = Executors.newFixedThreadPool(4)

  override def afterAll(): Unit = {
    super.afterAll()
    service.shutdownNow()
  }

  test("Monoid.bag")(genUnit) { _ =>
    assertEquals(bag(IndexedSeq.empty[String]), Map.empty[String, Int])
    assertEquals(bag(IndexedSeq("rose")), Map("rose" -> 1))
    assertEquals(bag(IndexedSeq("rose", "rose", "rose")), Map("rose" -> 3))
    assertEquals(bag(IndexedSeq("a", "rose", "is")), Map("a" -> 1, "rose" -> 1, "is" -> 1))
    assertEquals(bag(IndexedSeq("a", "rose", "is", "a", "rose")), Map("a" -> 2, "rose" -> 2, "is" -> 1))
  }

  test("Monoid.functionMonoid")(genInt) { a =>
    val m: Monoid[Int => String] = functionMonoid[Int, String]
    val f: Int => String = i => if i % 2 == 0 then "even" else "odd"
    val g: Int => String = i => if i < 0 then "negative" else "positive"
    val h: Int => String = i => i.toString

    assertEquals(m.combine(f, m.empty)(a), f(a), "identity")
    assertEquals(m.combine(m.empty, f)(a), f(a), "identity")
    assertEquals(m.combine(f, m.combine(g, h))(a), m.combine(m.combine(f, g), h)(a), "associativity")
  }

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
    val xs = IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    assertEquals(foldLeft(xs)(identity), 55)
  }
  test("E10.07 foldLeft using stringMonoid")(genUnit) { _ =>
    def toString = (i: Int) => i.toString
    val xs = IndexedSeq(1, 2, 3, 4, 5)

    assertEquals(foldLeft(xs)(toString), "12345")
  }
  test("E10.08 parFoldMap using stringMonoid")(genUnit) { _ =>

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
