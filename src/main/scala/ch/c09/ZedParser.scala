package ge.zgharbi.study.fps
package ch.c09

import ch.c09.Result.{Failure, Success}

import scala.annotation.tailrec
import scala.util.matching.Regex

case class ParseState(loc: Location, isSliced: Boolean) {
  def advanceBy(numChars: Int): ParseState =
    copy(loc = loc.advanceBy(numChars))

  def input: String = loc.input.substring(loc.offset)

  def unslice: ParseState = copy(isSliced = false)

  def reslice(s: ParseState): ParseState = copy(isSliced = s.isSliced)

  def slice(n: Int): String = loc.input.substring(loc.offset, loc.offset + n)
}

opaque type ZedParser[+A] = ParseState => Result[A]

enum Result[+A] {
  case Success(get: A, consumed: Int)
  case Failure(get: ParseError, isCommitted: Boolean) extends Result[Nothing]
  case Slice(length: Int) extends Result[String]

  def toEither(input: String): Either[ParseError, A] = this match
    case Failure(e, _) => Left(e)
    case Success(a, _) => Right(a)
    case Slice(length) => Right(input.substring(0, length))

  def slice: Result[String] = this match
    case s @ Slice(_) => s
    case Success(_, length) => Slice(length)
    case f @ Failure(_, _) => f

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
  import Result.*

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
    override def dotStar: ZedParser[List[A]] =
      s =>
        var nConsumed: Int = 0
        if s.isSliced then
          @tailrec
          def go(p: ZedParser[String], offset: Int): Result[String] =
            p(s.advanceBy(offset)) match
              case f @ Failure(e, true) => f
              case Failure(e, _) => Slice(offset)
              case Slice(n) => go(p, offset + n)
              case Success(_, _) =>
                sys.error("sliced parser should not return success, only slice")
          go(kore.slice, 0).asInstanceOf[Result[List[A]]]
        else
          val buf = new collection.mutable.ListBuffer[A]
          @tailrec
          def go(p: ZedParser[A], offset: Int): Result[List[A]] =
            p(s.advanceBy(offset)) match
              case Success(a, n) =>
                buf += a
                go(p, offset + n)
              case f @ Failure(e, true) => f
              case Failure(e, _) => Success(buf.toList, offset)
              case Slice(n) =>
                buf += s.input.substring(offset, offset + n)
                go(p, offset + n)
          go(kore, 0)

    override def slice: ZedParser[String] =
      state =>
        kore(state.copy(isSliced = true)) match
          case s @ Slice(_) => s
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
      kore(state.unslice) match
        case Slice(length) =>
          f(state.slice(length))(state.advanceBy(length).reslice(state))
            .advanceSuccess(length)
        case Success(a, n) =>
          f(a)(state.advanceBy(n).reslice(state))
            .addCommit(n != 0)
            .advanceSuccess(n)
        case f @ Failure(_, _) => f

    override def or(sore: => ZedParser[A]): ZedParser[A] = loc =>
      kore(loc) match
        case Failure(_, false) => sore(loc)
        case r => r

    override def run(input: String): Either[ParseError, A] = kore(
      ParseState(Location(input), false),
    ).toEither(input)
  }
}
