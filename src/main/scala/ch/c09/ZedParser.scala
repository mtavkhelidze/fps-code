package ge.zgharbi.study.fps
package ch.c09

import ch.c09.Result.{Failure, Success}

import scala.annotation.tailrec
import scala.util.matching.Regex

case class ParseState(loc: Location, isSliced: Boolean)

opaque type ZedParser[+A] = ParseState => Result[A]

enum Result[+A] {
  case Success(get: A, consumed: Int)
  case Failure(get: ParseError, isCommitted: Boolean) extends Result[Nothing]

  def extract: Either[ParseError, A] = this match
    case Failure(e, _) => Left(e)
    case Success(a, _) => Right(a)

  def mapError(f: ParseError => ParseError): Result[A] = this match {
    case Failure(e, c) => Failure(f(e), c)
    case _ => this
  }

  def addCommit(isCommitted: Boolean): Result[A] = this match {
    case Failure(e, c) => Failure(e, c || isCommitted)
    case _ => this
  }
  def unCommit: Result[A] = this match
    case Failure(e, _) => Failure(e, false)
    case _ => this

  def advanceSuccess(n: Int): Result[A] = this match
    case Success(a, m) => Success(a, n + m)
    case _ => this
}

object ZedParser extends Parsers[ZedParser] {
  val nonNegativeInt: ZedParser[Int] =
    for
      nString <- regex("[0-9]+".r)
      n <- nString.toIntOption match
        case Some(n) => succeed(n)
        case None => fail("expected an integer")
    yield n

  val nonNegativeIntOpaque: ZedParser[Int] =
    nonNegativeInt.label("non-negative integer")

  override def string(s: String): ZedParser[String] = state =>
    val i = firstNonMatchingIndex(state.loc.input, s, state.loc.offset)
    if i == -1
    then Result.Success(s, s.length)
    else Result.Failure(state.loc.advanceBy(i).toError(s"Expected: $s"), i != 0)

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

  override def regex(r: Regex): ZedParser[String] =
    state =>
      r.findPrefixOf(state.loc.remaining) match
        case None => Failure(state.loc.toError(s"regex $r"), false)
        case Some(m) => Success(m, m.length)

  override def succeed[A](a: A): ZedParser[A] = _ => Success(a, 0)

  override def fail(msg: String): ZedParser[Nothing] = state =>
    Failure(state.loc.toError(msg), false)

  extension [A](kore: ZedParser[A]) {
    override def many: ZedParser[List[A]] =
      state =>
        val buf = new collection.mutable.ListBuffer[A]

        @tailrec
        def go(p: ZedParser[A], offset: Int): Result[List[A]] =
          p(ParseState(state.loc.advanceBy(offset), false)) match
            case Success(a, n) =>
              buf += a
              go(p, offset + n)
            case Failure(e, true) => Failure(e, true)
            case Failure(_, _) => Success(buf.toList, offset)

        go(kore, 0)

    override def slice: ZedParser[String] =
      state =>
        kore(state) match
          case Success(_, n) =>
            Success(
              state.loc.input.substring(state.loc.offset, state.loc.offset + n),
              n,
            )
          case f @ Failure(_, _) => f

    override def scope(msg: String): ZedParser[A] =
      state => kore(state).mapError(_.push(state.loc, msg))

    override def label(l: String): ZedParser[A] =
      loc => kore(loc).mapError(_.label(l))

    override def attempt: ZedParser[A] = loc => kore(loc).unCommit

    override def flatMap[B](f: A => ZedParser[B]): ZedParser[B] = state =>
      kore(state) match
        case Success(a, n) =>
          f(a)(ParseState(state.loc.advanceBy(n), false))
            .addCommit(n == 0)
            .advanceSuccess(n)
        case f @ Failure(_, _) => f

    override def or(sore: => ZedParser[A]): ZedParser[A] = loc =>
      kore(loc) match
        case Failure(_, false) => sore(loc)
        case r => r

    override def run(input: String): Either[ParseError, A] = kore(
      ParseState(Location(input), false),
    ).extract
  }
}
