package ge.zgharbi.study.fps
package ex

import ch.c08Testing.exhaustive.Gen.**
import ch.c11Monad.Monad
import common.Common.{genInt, genString}
import common.PropSuite

import munit.FunSuite

class E11MonadLawsSuite extends PropSuite {
  test("Option")(genInt ** genInt ** genInt) { case i ** j ** k =>
    import Monad.optionMonad
    val m = optionMonad.unit(i)

    def f(n: Int) = Some(j + 1)

    def g(n: Int) = Some(k + 2)

    val expected = m.flatMap(f).flatMap(g)
    val actual = m.flatMap(n => f(n).flatMap(g))
    assertEquals(actual, expected, "associativity")
  }
}

class E11MonadSuite extends PropSuite {
  private def assertMonad[F[_], A](monad: Monad[F], fn: Int => F[Int], n: Int, s: String): Unit = {
    assertEquals(monad.unit(n), fn(n))
    assertFlatMap(monad, n)
    assertMap(monad, n)
    assertMap2(monad, n, s)
  }

  private def assertFlatMap[F[_]](m: Monad[F], n: Int): Unit = {
    val actual = m.flatMap(m.unit(n))(i => m.unit(i + 1))
    val expected = m.unit(n + 1)
    assertEquals(actual, expected)
  }

  private def assertMap[F[_]](m: Monad[F], n: Int): Unit = {
    val actual = m.map(m.unit(n))(i => i + 1)
    val expected = m.unit(n + 1)
    assertEquals(actual, expected)
  }

  private def assertMap2[F[_]](m: Monad[F], n: Int, s: String): Unit = {
    val actual = m.map2(m.unit(n))(m.unit(s))((i, s) => i + s.length)
    val expected = m.unit(n + s.length)
    assertEquals(actual, expected)
  }

  test("11.01 Monad#optionMonad")(genInt ** genString) { case n ** s =>
    import Monad.optionMonad
    assertMonad(optionMonad, Some.apply, n, s)
  }

}
