package ge.zgharbi.study.fps
package ch.ch09

type Parser[+A]
type ParserError

object Parser {
  extension [A](p: Parser[A]) {
    def run(input: String): Either[ParserError, A] = ???
  }

  def char(c: Char): Parser[Char] = ???
}
