package ge.zgharbi.study.fps
package ex

import ch.c08.{Gen, Prop}
import ch.c08.Prop.{verify, MaxSize, Result, TestCases}
import ch.c08.Prop.Result.Falsified

import munit.FunSuite

class E08Test extends FunSuite {
  test("verify") {
    verify(true).run(MaxSize.fromInt(1), TestCases.fromInt(100))
  }

  test("maxProp") {
    val smallInt = Gen.choose(10, 100)
    val maxProp = Prop.forAll(smallInt.nonEmptyList) { ns =>
      val max = ns.max
      !ns.exists(_ > max)
    }

    maxProp.check() match
      case Falsified(msg, nc) => fail(s"After $nc: $msg")
      case _ => ()
  }
}
