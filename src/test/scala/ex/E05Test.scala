package ge.zgharbi.study.fps
package ex

import munit.FunSuite

class E05Test extends FunSuite {

  import ch.c05.LazyList
  import ch.c05.LazyList.*

  test("05.01 LazyList#toList") {
    val actual = LazyList(1, 2, 3, 4).toList
    val expected = List(1, 2, 3, 4)
    assertEquals(actual, expected)
  }

  test("05.02 LazyList#drop") {
    val actual = LazyList(1, 2, 3, 4, 5).drop(3).toList
    val expected = LazyList(4, 5).toList
    assertEquals(actual, expected)
  }

  test("05.02 LazyList#take") {
    val actual = LazyList(1, 2, 3, 4, 5).take(3).toList
    val expected = LazyList(1, 2, 3).toList
    assertEquals(actual, expected)
  }

  test("05.03 LazyList#takeWhile") {
    val pred = (n: Int) => n % 2 == 0
    val actual = LazyList(2, 2, 2, 4, 5).takeWhile(pred).toList
    val expected = LazyList(2, 2, 2, 4).toList
    assertEquals(actual, expected)
  }

  test("05 LazyList#exists") {
    val isThree = (n: Int) => n == 3
    val actual = LazyList(2, 2, 3, 4, 5).exists(isThree)
    val expected = true
    assertEquals(actual, expected)
  }

  test("05.04 LazyList#forAll false") {
    val isThree = (n: Int) => n == 3
    val actual = LazyList(2, 2, 3, 4, 5).forAll(isThree)
    val expected = false
    assertEquals(actual, expected)
  }

  test("05.04 LazyList#forAll true") {
    val lessThanTen = (n: Int) => n < 10
    val actual = LazyList(2, 2, 3, 4, 5).forAll(lessThanTen)
    val expected = true
    assertEquals(actual, expected)
  }

  test("05.06 LazyList#headOption") {
    {
      val actual = LazyList(10, 20, 30).headOption
      val expected = Some(10)
      assertEquals(actual, expected)
    }
    {
      val actual = empty.headOption
      val expected = None
      assertEquals(actual, expected)
    }
  }

  test("05.12 continually using unfold") {
    val actual = continually(10).take(5).toList
    val expected = List(10, 10, 10, 10, 10)
    assertEquals(actual, expected)
  }

  test("05.12 from using unfold") {
    val actual = from(10).take(5).toList
    val expected = List(10, 11, 12, 13, 14)
    assertEquals(actual, expected)
  }

  test("05.12 fibs") {
    val actual = fibs.take(10).toList
    val expected = List(0, 1, 1, 2, 3, 5, 8, 13, 21, 34)
    assertEquals(actual, expected)
  }

  test("05.13 zipWith using unfold") {
    val as = LazyList(1, 2, 3)
    val bs = LazyList("One", "Two", "Three", "Four")
    val transf = (a: Int, b: String) => (a, b)
    val actual = as.zipWith(bs)(transf).toList
    val expected = List((1, "One"), (2, "Two"), (3, "Three"))
    assertEquals(actual, expected)
  }

  test("05.13 zipAll using unfold") {
    val as = LazyList(1, 2)
    val bs = LazyList("One", "Two", "Three")
    val actual = as.zipAll(bs).toList
    val expected = List(
      Some(1) -> Some("One"),
      Some(2) -> Some("Two"),
      None -> Some("Three"),
    )
    assertEquals(actual, expected)
  }

  test("05.14 startsWith false") {
    val actual = LazyList(1, 2, 3).startsWith(LazyList(4, 5))
    val expected = false
    assertEquals(actual, expected)
  }

  test("05.14 startsWith true") {
    val actual = LazyList(1, 2, 3).startsWith(LazyList(1, 2))
    val expected = true
    assertEquals(actual, expected)
  }

  test("05.15 LazyList#tails") {
    val actual = LazyList(1, 2, 3).tails.map(_.toList).toList
    val expected = List(List(1, 2, 3), List(2, 3), List(3), Nil)
    assertEquals(actual, expected)
  }

  test("05 LazyList#hasSubSequence") {
    {
      val actual = LazyList(1, 2, 7, 4, 5).hasSubSequence(LazyList(7, 4))
      val expected = true
      assertEquals(actual, expected)
    }
    {
      val actual = LazyList(1, 2, 7, 4, 5).hasSubSequence(LazyList(12, 13))
      val expected = false
      assertEquals(actual, expected)
    }
  }

  test("05.16 LazyList#scanRight") {
    val actual = LazyList(1, 2, 3).scanRight(0)(_ + _).toList
    val expected = List(6, 5, 3, 0)
    assertEquals(actual, expected)
  }
}
