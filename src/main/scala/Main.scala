package ge.zgharbi.study.fps

import ch.c07Parallelism.NonBlocking.Par
import ch.c10.Monoid
import ch.c10.Monoid.{parFoldMap, stringMonoid}

import java.util.concurrent.{Executors, ExecutorService, Future as JavaFuture, *}
import java.util.concurrent.TimeUnit.{MICROSECONDS, MILLISECONDS}

object Main extends App {
  lazy val service: ExecutorService = Executors.newFixedThreadPool(4)
  val xs = 1 to 5
  val actual: Par[String] = parFoldMap(xs)(ts)
  val value = actual.run(service)

  def ts(i: Int) = i.toString

  given monoid: Monoid[String] = stringMonoid

  println(value)
  service.shutdown()
}
