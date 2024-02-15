package ge.zgharbi.study.fps
package ex

import munit.FunSuite
import org.junit.Assert.*

class E02Test extends FunSuite {
  import E02.*
  test("isSorted") {
    assertTrue(isSorted(Array(1, 2, 3), _ < _))
    assertTrue(isSorted(Array(3, 2, 1), _ > _))
    assertFalse(isSorted(Array(1, 2, 1), _ < _))
    assertFalse(isSorted(Array(1, 2, 3), _ > _))
  }
}
