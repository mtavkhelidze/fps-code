package ge.zgharbi.study.fps
package ch.ch09

import ch.c08.{Gen, Prop}

import scala.annotation.targetName
import scala.util.matching.Regex

//noinspection ScalaWeakerAccess,ScalaUnusedSymbol
trait Parsers[ParserError, Parser[+_]] {
  extension [A](kore: Parser[A]) {
    def flatMap[B](f: A => Parser[B]): Parser[B]

    def product[B](sore: => Parser[B]): Parser[(A, B)]
    @targetName("productParser")
    infix def **[B](sore: Parser[B]): Parser[(A, B)] = product(sore)

    def slice: Parser[String]

    infix def map[B](f: A => B): Parser[B]

    def run(input: String): Either[ParserError, A]

    @targetName("orParser")
    def |(sore: => Parser[A]): Parser[A] = kore or sore
    infix def or(sore: => Parser[A]): Parser[A]

    def listOfN(n: Int): Parser[List[A]] =
      if n <= 0 then succeed(Nil) else kore.map2(listOfN(n - 1))(_ :: _)

    def numOf(c: Char): Parser[Int] = char(c).many.map(_.size)

    def many: Parser[List[A]] =
      kore.map2(kore.many)(_ :: _) | succeed(Nil)

    def oneOrMany: Parser[List[A]] =
      kore.map2(kore.many)(_ :: _)

    def map2[B, C](sore: => Parser[B])(f: (A, B) => C): Parser[C] =
      kore ** sore map f.tupled

    def string(s: String): Parser[String]

    def fail(msg: String): Parser[Nothing]

    def succeed[T](a: T): Parser[T]

    def defaultSucceed[T](a: T): Parser[T] =
      string("").map(_ => a)

    def char(c: Char): Parser[Char] = string(c.toString).map(_.head)

    def regex(r: Regex): Parser[String]
  }

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
