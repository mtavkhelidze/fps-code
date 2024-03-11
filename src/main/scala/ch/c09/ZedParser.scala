package ge.zgharbi.study.fps
package ch.c09

import ch.ch09.{Location, ParseError, Parsers}

import scala.util.matching.Regex

class ZedParser[+A](loc: Location) {}

object ZedParser extends Parsers[ZedParser] {
  override def string(s: String): ZedParser[String] =
    new ZedParser[String](Location(s))

  override def regex(r: Regex): ZedParser[String] = ???

  extension [A](kore: ZedParser[A])
    override def slice: ZedParser[String] = ???
    override def label(s: String): ZedParser[A] = ???
    override def scope(msg: String): ZedParser[A] = ???
    override def flatMap[B](f: A => ZedParser[B]): ZedParser[B] = ???
    override def attempt: ZedParser[A] = ???
    override infix def or(sore: => ZedParser[A]): ZedParser[A] = ???
    override def succeed[T](a: T): ZedParser[T] = ???
    override def run(input: String): Either[ParseError, A] = ???
    override def fail(msg: String): ZedParser[Nothing] = ???
}
