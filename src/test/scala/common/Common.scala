package ge.zgharbi.study.fps
package common

import ch.c06State.RNG
import ch.c08Testing.Gen

import java.util.concurrent.{Executors, ExecutorService}

object Common {
  lazy val service: ExecutorService = Executors.newFixedThreadPool(4)
  lazy val genChar: Gen[Char] = Gen.choose(97, 123).map(_.toChar)
  lazy val genString: Gen[String] = genList(genChar).map(_.mkString)
  lazy val genRNG: Gen[RNG] = Gen.int.map(i => RNG.Simple(i.toLong))
}
