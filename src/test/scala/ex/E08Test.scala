package ge.zgharbi.study.fps
package ex

import ch.c08.{Gen, Prop}
import ch.c08.Prop.{MaxSize, TestCases}

import munit.FunSuite

class E08Test extends FunSuite {
  test("maxProp") {
    val smallInt = Gen.choose(-10, 10)
    val maxProp = Prop.forAll(smallInt.nonEmptyList) { ns =>
      val max = ns.max
      ns.exists(_ > max)
    }

    maxProp.run(MaxSize.fromInt(21), TestCases.fromInt(13))
  }
}
