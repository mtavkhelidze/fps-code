package ge.zgharbi.study.fps
package ch.c09

import ch.c09.Result.{Failure, Success}

import scala.util.matching.Regex

opaque type ZedParser[+A] = Location => Result[A]

enum Result[+A] {
  case Success(get: A, consumed: Int)
  case Failure(get: ParseError, isCommitted: Boolean) extends Result[Nothing]

  def mapError(f: ParseError => ParseError): Result[A] = this match {
    case Failure(e, c) => Failure(f(e), c)
    case _ => this
  }
}

object ZedParser extends Parsers[ZedParser] {
  val nonNegativeInt: ZedParser[Int] =
    for
      nString <- regex("[0-9]+".r)
      n <- nString.toIntOption match
        case Some(n) => succeed(n)
        case None => fail("expected an integer")
    yield n

  override def string(s: String): ZedParser[String] = loc =>
    val i = firstNonMatchingIndex(loc.input, s, loc.offset)
    if i == -1
    then Result.Success(s, s.length)
    else Result.Failure(loc.advanceBy(i).toError(s"Expected: $s"), i != 0)

  private def firstNonMatchingIndex(
      input: String,
      needle: String,
      offset: Int,
  ): Int = {
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

  override def succeed[A](a: A): ZedParser[A] = _ => Success(a, 0)

  override def fail(msg: String): ZedParser[Nothing] = ???

  extension [A](kore: ZedParser[A]) {

    override def slice: ZedParser[String] =
      loc =>
        kore(loc) match
          case Success(_, n) =>
            Success(loc.input.substring(loc.offset, loc.offset + n), n)
          case f @ Failure(_, _) => f

    override def scope(s: String): ZedParser[A] =
      loc => kore(loc).mapError(_.push(loc, s))

    override def label(l: String): ZedParser[A] =
      loc => kore(loc).mapError(_.label(l))

    override def attempt: ZedParser[A] = ???

    override def flatMap[B](f: A => ZedParser[B]): ZedParser[B] = ???

    override def or(sore: => ZedParser[A]): ZedParser[A] = ???

    override def run(input: String): Either[ParseError, A] = ???
  }
}
