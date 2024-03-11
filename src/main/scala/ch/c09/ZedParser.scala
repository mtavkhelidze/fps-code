package ge.zgharbi.study.fps
package ch.c09

import ch.ch09.{Location, ParseError, Parsers}

import scala.util.matching.Regex

opaque type ZedParser[+A] = Location => Result[A]

enum Result[+A] {
  case Success(get: A, consumed: Int)
  case Failure(get: ParseError) extends Result[Nothing]
}

object ZedParser extends Parsers[ZedParser] {
  override def string(s: String): ZedParser[String] = input =>
    if s.startsWith(s) then Right(s)
    else Left(Location(input).toError(s"Expected: $s"))

  override def regex(r: Regex): ZedParser[String] = ???

  extension [A](kore: ZedParser[A])
    override def attempt: ZedParser[A] = ???
    override def fail(msg: String): ZedParser[Nothing] = ???
    override def flatMap[B](f: A => ZedParser[B]): ZedParser[B] = ???

    override def label(l: String): ZedParser[A] = ???
    override def or(sore: => ZedParser[A]): ZedParser[A] = ???
    override def run(input: String): Either[ParseError, A] = ???
    override def scope(s: String): ZedParser[A] = ???
    override def slice: ZedParser[String] = ???
    override def succeed[T](a: T): ZedParser[T] = ???
}
