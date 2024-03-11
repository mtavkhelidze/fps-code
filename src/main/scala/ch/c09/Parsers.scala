//noinspection ScalaWeakerAccess,ScalaUnusedSymbol
package ge.zgharbi.study.fps
package ch.ch09

import ch.c08.{Gen, Prop}

import java.util.regex.Pattern
import scala.annotation.targetName
import scala.util.matching.Regex

trait Parsers[Parser[+_]] {
  def string(s: String): Parser[String]

  def regex(r: Regex): Parser[String]

  def double: Parser[Double] =
    doubleString.map(_.toDouble).label("double literal")

  def doubleString: Parser[String] =
    regex("[-+]?([0-9]*\\.)?[0-9]+([eE][-+]?[0-9]+)?".r).token

  def escapedQuoted: Parser[String] = quoted.label("string literal").token

  /** Unescaped string literals, like "foo" or "bar". */
  def quoted: Parser[String] = string("\"") *> thru("\"").map(_.dropRight(1))

  /** Parser which consumes reluctantly until it encounters the given string. */
  def thru(s: String): Parser[String] = regex((".*?" + Pattern.quote(s)).r)

  def whitespace: Parser[String] = regex("[ \\t\\n\\r]+".r)

  def eof: Parser[String] =
    regex("\\z".r).label("unexpected trailing characters")

  extension [A](kore: Parser[A]) {
    def slice: Parser[String]

    def label(s: String): Parser[A]

    def scope(msg: String): Parser[A]

    def flatMap[B](f: A => Parser[B]): Parser[B]

    def attempt: Parser[A]

    infix def or(sore: => Parser[A]): Parser[A]

    def token: Parser[A] = kore.attempt <* whitespace

    def as[B](b: B): Parser[B] = kore.slice.map(_ => b)

    def sep(separator: Parser[Any]): Parser[List[A]] =
      kore.sep1(separator) | succeed(Nil)

    def sep1(separator: Parser[Any]): Parser[List[A]] =
      kore.map2((separator *> kore).many)(_ :: _)

    def succeed[T](a: T): Parser[T]

    def product[B](sore: => Parser[B]): Parser[(A, B)] =
      kore.flatMap(a => sore.map(b => (a, b)))

    @targetName("orParser")
    infix def |(sore: => Parser[A]): Parser[A] = kore or sore

    @targetName("productParser")
    infix def **[B](sore: Parser[B]): Parser[(A, B)] = product(sore)

    @targetName("keepRight")
    infix def *>[B](sore: => Parser[B]): Parser[B] =
      kore.map2(sore)((_, x) => x)

    @targetName("keepLeft")
    infix def <*[B](sore: => Parser[B]): Parser[A] =
      kore.map2(sore)((x, _) => x)

    def map2[B, C](sore: => Parser[B])(f: (A, B) => C): Parser[C] =
      kore.flatMap(a => sore.map(b => f(a, b)))

    infix def map[B](f: A => B): Parser[B] =
      kore.flatMap(a => succeed(f(a)))

    def run(input: String): Either[ParseError, A]

    def listOfN(n: Int): Parser[List[A]] =
      if n <= 0 then succeed(Nil) else kore.map2(listOfN(n - 1))(_ :: _)

    def numOf(c: Char): Parser[Int] = char(c).many.map(_.size)

    def many: Parser[List[A]] =
      kore.map2(kore.many)(_ :: _) | succeed(Nil)

    def oneOrMany: Parser[List[A]] =
      kore.map2(kore.many)(_ :: _)

    def defaultSucceed[T](a: T): Parser[T] =
      string("").map(_ => a)

    def fail(msg: String): Parser[Nothing]

    def char(c: Char): Parser[Char] = string(c.toString).map(_.head)
  }

  object Laws {
    def unBiasL[A, B, C](t: ((A, B), C)): (A, B, C) = (t._1._1, t._1._2, t._2)
    def unBiasR[A, B, C](t: (A, (B, C))): (A, B, C) = (t._1, t._2._1, t._2._2)

    def mapLaw[A](p: Parser[A])(in: Gen[String]): Prop =
      equal(p, p.map(identity))(in)

    def orLaw[A](p: Parser[A], p2: Parser[A])(in: Gen[String]): Prop =
      equal(p or p2, p.map(identity) or p2.map(identity))(in)

    def equal[A](p1: Parser[A], p2: Parser[A])(in: Gen[String]): Prop =
      Prop.forAll(in)(s => p1.run(s) == p2.run(s))

    def charLas(p: Parser[Char])(in: Gen[Char]): Prop =
      Prop.forAll(in)(c => p.run(c.toString).isRight)

    def stringLas(p: Parser[String])(in: Gen[String]): Prop =
      Prop.forAll(in)(c => p.run(c).isRight)
  }
}

case class ParseError(stack: List[(Location, String)]) {
  def push(loc: Location, msg: String): ParseError =
    copy(stack = (loc, msg) :: stack)

  def label(s: String): ParseError =
    ParseError(latestLoc.map((_, s)).toList)

  def latestLoc: Option[Location] =
    if stack.isEmpty then None else Some(stack.head._1)

  def latest: Option[(Location, String)] =
    stack.lastOption

  override def toString: String =
    if stack.isEmpty then "no error message"
    else
      val collapsed = collapseStack(stack)
      val context =
        collapsed.lastOption.map("\n\n" + _._1.currentLine).getOrElse("") +
          collapsed.lastOption.map("\n" + _._1.columnCaret).getOrElse("")
      collapsed
        .map((loc, msg) => s"${formatLoc(loc)} $msg")
        .mkString("\n") + context

  def formatLoc(l: Location): String = s"${l.line}.${l.col}"

  def collapseStack(s: List[(Location, String)]): List[(Location, String)] =
    s.groupBy(_._1)
      .view
      .mapValues(_.map(_._2).mkString("; "))
      .toList
      .sortBy(_._1.offset)
}

case class Location(input: String, offset: Int = 0) {
  lazy val line: Int = input.slice(0, offset + 1).count(_ == '\n') + 1
  lazy val col: Int = input.slice(0, offset + 1).lastIndexOf('\n') match {
    case -1 => offset + 1
    case lineStart => offset - lineStart
  }

  def currentLine: String =
    if input.length > 1
    then
      val itr = input.linesIterator.drop(line - 1)
      if itr.hasNext then itr.next() else ""
    else ""

  def columnCaret: String = (" " * (col - 1)) + "^"
}
