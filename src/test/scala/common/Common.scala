package ge.zgharbi.study.fps
package common

import ch.c06State.RNG
import ch.c08Testing.exhaustive.Gen

import java.util.concurrent.{Executors, ExecutorService}

object Common {

  lazy val genUnit: Gen[Unit] = Gen.unit(())
  lazy val genInt: Gen[Int] = Gen.int
  lazy val genChar: Gen[Char] = Gen.choose(97, 123).map(_.toChar)
  lazy val genRNG: Gen[RNG] = Gen.int.map(i => RNG.Simple(i.toLong))
  lazy val genShortNumber: Gen[Int] = Gen.choose(0, 20)
  lazy val genString: Gen[String] = genList(genChar).map(_.mkString)

  lazy val service: ExecutorService = Executors.newFixedThreadPool(4)

  def genList[A](g: Gen[A]): Gen[List[A]] =
    for
      n <- genShortNumber
      list <- Gen.listOfN(n, g)
    yield list
}
