package ge.zgharbi.study.fps
package ex

import ch.c06.CandyMachine

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

  test("06.04 RNG#ints") {
    val actual = rng.ints(10)._1
    val expected = List(
      119411244, 234161297, -1244417173, 1634559674, 946775486, -1754112425,
      538511747, 1640547667, -1611436125, 231721747,
    )
    assertEquals(actual, expected)
  }

  test("06.11 CandyMachine") {
    import ch.c06.CandyMachine.*
    import ch.c06.CandyMachine.Input.*

    val m0 = Machine(locked = true, coins = 10, candies = 5)
    val inputs = List(Coin, Turn, Coin, Turn, Coin, Turn, Coin, Turn)
    val (_, actual) = simulateMachine(inputs).run(m0)
    val expected = Machine(true, 1, 14)
    assertEquals(actual, expected)
  }
}
