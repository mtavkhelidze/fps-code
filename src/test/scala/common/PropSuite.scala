package ge.zgharbi.study.fps
package common

import ch.c08Testing.exhaustive.{Gen, Prop}
import ch.c08Testing.exhaustive.Prop.Result
import ch.c08Testing.exhaustive.Prop.Result.*

import munit.{FunSuite, Location, TestOptions}
import munit.internal.FutureCompat.*
import scala.annotation.nowarn
import scala.util.{Failure, Success, Try}
trait PropSuite extends FunSuite {
  private val scalaCheckPropTransform: TestTransform =
    new TestTransform(
      "PropTest ",
      t =>
        t.withBodyMap(
          _.transformCompat {
            case Success(result: Result @nowarn) => resultToTry(result, t)
            case r => r
          }(munitExecutionContext),
        ),
    )

  def test[A](name: String)(gen: Gen[A])(f: A => Unit)(using
      loc: Location,
  ): Unit = {
    val g: A => Boolean = a => {
      f(a)
      true
    }
    val prop = Prop.forAll(gen)(g)
    test(new TestOptions(name, Set.empty, loc))(prop.check())
  }

  override def munitTestTransforms: List[TestTransform] =
    super.munitTestTransforms :+ scalaCheckPropTransform

  private def resultToTry(result: Result, test: Test): Try[Unit] =
    result match
      case Passed(status, n) =>
        println(
          s"${test.name}: + OK, property ${status.toString.toLowerCase}, ran $n tests.",
        )
        Success(())
      case Falsified(msg) =>
        Try(fail(msg.string)(test.location))
}
