package ge.zgharbi.study.fps
package ch.ch09

import ch.c08.{Gen, Prop}

import scala.annotation.targetName

//noinspection ScalaWeakerAccess,ScalaUnusedSymbol
trait Parsers[ParserError, Parser[+_]] {
  extension [A](kore: Parser[A]) {
    def product[B](sore: Parser[B]): Parser[(A, B)] = ???
    @targetName("productParser")
    infix def **[B](sore: Parser[B]): Parser[(A, B)] = product(sore)

    def slice: Parser[String] = ???

    infix def map[B](f: A => B): Parser[B] = ???

    def run(input: String): Either[ParserError, A] = ???

    @targetName("orParser")
    def |(sore: Parser[A]): Parser[A] = kore or sore
    infix def or(sore: Parser[A]): Parser[A] = ???

    def listOfN(n: Int): Parser[List[A]] = ???

    def numOf(c: Char): Parser[Int] = char(c).many.map(_.size)
  }

  def many1[A](p: Parser[A]): Parser[List[A]] =
    map2(p, many(p))(_ :: _)

  def map2[A, B, C](p1: Parser[A], p2: Parser[B])(f: (A, B) => C): Parser[C] =
    p1 ** p2 map f.tupled

  def many[A](p: Parser[A]): Parser[List[A]] = ???

  def succeed[A](a: A): Parser[A] = string("").map(_ => a)

  def string(s: String): Parser[String] = ???

  def countChars(c: Char): Parser[Int] = ???

  def char(c: Char): Parser[Char] = string(c.toString).map(_.head)

  def startingWith(c: Char): Parser[String] = ???

  object Laws {
    def unBiasL[A, B, C](t: ((A, B), C)): (A, B, C) = (t._1._1, t._1._2, t._2)
    def unBiasR[A, B, C](t: (A, (B, C))): (A, B, C) = (t._1, t._2._1, t._2._2)

    def mapLaw[A](p: Parser[A])(in: Gen[String]): Prop =
      equal(p, p.map(identity))(in)

    def equal[A](p1: Parser[A], p2: Parser[A])(in: Gen[String]): Prop =
      Prop.forAll(in)(s => p1.run(s) == p2.run(s))

    def orLaw[A](p: Parser[A], p2: Parser[A])(in: Gen[String]): Prop =
      equal(p or p2, p.map(identity) or p2.map(identity))(in)

    def charLas(p: Parser[Char])(in: Gen[Char]): Prop =
      Prop.forAll(in)(c => p.run(c.toString).isRight)

    def stringLas(p: Parser[String])(in: Gen[String]): Prop =
      Prop.forAll(in)(c => p.run(c).isRight)
  }
}
