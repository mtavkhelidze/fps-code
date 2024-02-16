package ge.zgharbi.study.fps
package ex

import munit.FunSuite

class E04Test extends FunSuite {
  import ch.c04.Option
  import ch.c04.Option.*

  test("04.02 variance") {
    import ex.E04.variance

    val v = variance(Seq(1.0, 2.0, 3.0, 4.0, 5.0))
    assertEquals(v, Some(2.0))
    assertEquals(variance(Nil), None)
  }

  test("04.03 Option#map2") {
    val a = Some(10)
    val b = Some(20)
    assertEquals(map2(a, b)(_ + _), Some(30))
  }

  test("04.03 Option#map2") {
    val as = List(Some(10), Some(20), Some(30))
    assertEquals(sequence(as), Some(List(10, 20, 30)))

    val bs = Some(11) :: None :: as
    assertEquals(sequence(bs), None)
  }
}
