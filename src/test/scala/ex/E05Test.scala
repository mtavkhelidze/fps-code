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
}
