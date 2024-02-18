package ge.zgharbi.study.fps
package ex

import munit.FunSuite

class E06Test extends FunSuite {
  import ch.c06.RNG.*

  val seed = 0xdeadbeefL
  val rng: SimpleRNG = SimpleRNG(seed)

  test("06.01 RNG#nonNegativeInt") {
    val actual = rng.nonNegativeInt._1
    val expected = 231721747
    assertEquals(actual, expected)
  }
}
