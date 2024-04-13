package ge.zgharbi.study.fps
package ex

import ch.c08Testing.{Gen, Prop}
import ch.c08Testing.Prop.{verify, MaxSize, Result, TestCases}
import ch.c08Testing.Prop.Result.Falsified

import munit.FunSuite

class E08Test extends FunSuite {
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
