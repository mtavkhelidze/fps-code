package ge.zgharbi.study.fps
package ex

import munit.FunSuite
import org.junit.Assert
import org.junit.Assert.*

class E02Test extends FunSuite {
  import E02.*
  test("02.02 isSorted") {
    assertTrue(isSorted(Array(1, 2, 3), _ < _))
    assertTrue(isSorted(Array(3, 2, 1), _ > _))
    assertFalse(isSorted(Array(1, 2, 1), _ < _))
    assertFalse(isSorted(Array(1, 2, 3), _ > _))
  }

  test("02.03 curry") {
    def mult(a: Int, b: Int): Int = a * b
    def curriedMult = curry(mult)
    assertEquals(mult(2, 3), curriedMult(2)(3))
  }

  test("02.04 uncurry") {
    def curriedMult(a: Int)(b: Int): Int = a + b
    def mult = uncurry(curriedMult)
    assertEquals(5, mult(2, 3))

  }
}
