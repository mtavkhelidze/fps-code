package ge.zgharbi.study.fps
package ch.c09

import ch.c09.Result.Failure
import ch.ch09.{Location, ParseError, Parsers}

import scala.util.matching.Regex

opaque type ZedParser[+A] = Location => Result[A]

enum Result[+A] {
  case Success(get: A, consumed: Int)
  case Failure(get: ParseError, isCommitted: Boolean) extends Result[Nothing]
}

object ZedParser extends Parsers[ZedParser] {
  override def string(s: String): ZedParser[String] = loc =>
    val i = firstNonMatchingIndex(loc.input, s, loc.offset)
    if i == -1
    then Result.Success(s, s.length)
    else Result.Failure(loc.advanceBy(i).toError(s"Expected: $s"), i != 0)

  private def firstNonMatchingIndex(input: String, needle: String, offset: Int): Int = {
    var i = 0
    while i + offset < input.length && i < needle.length do
      if input(i + offset) != needle(i) then return i
      i += 1
    if input.length - offset >= needle.length then -1
    else input.length - offset
  }

  override def regex(r: Regex): ZedParser[String] = loc =>
    r.findPrefixOf(loc.input.substring(loc.offset)) match {
      case Some(m) => Result.Success(m, m.length)
      case None => Failure(loc.toError(s"Expected: $r"), false)
    }

  extension [A](kore: ZedParser[A]) {
    override def succeed[T](a: T): ZedParser[T] =
      _ => Result.Success(a, 0)

    override def slice: ZedParser[String] =
      loc => Result.Success(loc.input.take(loc.offset), loc.offset)

    override def attempt: ZedParser[A] = ???

    override def fail(msg: String): ZedParser[Nothing] = ???

    override def flatMap[B](f: A => ZedParser[B]): ZedParser[B] = ???

    override def label(l: String): ZedParser[A] = ???

    override def or(sore: => ZedParser[A]): ZedParser[A] = ???

    override def run(input: String): Either[ParseError, A] = ???

    override def scope(s: String): ZedParser[A] = ???
  }
}
