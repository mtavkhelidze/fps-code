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
}
