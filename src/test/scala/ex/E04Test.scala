package ge.zgharbi.study.fps
package ex

import munit.FunSuite

class E04Test extends FunSuite {
  import ch.c04.Option.*
  import ex.E04.variance

  test("04.02 variance") {
    val v = variance(Seq(1.0, 2.0, 3.0, 4.0, 5.0))
    assertEquals(v, Some(2.0))
    assertEquals(variance(Nil), None)
  }
}
