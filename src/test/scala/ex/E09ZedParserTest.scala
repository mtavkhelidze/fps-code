package ge.zgharbi.study.fps
package ex

import ch.c09.{JSONGrammar, ZedParser}
import ch.c09.JSONGrammar.{JArray, JNumber}

import munit.FunSuite

class E09ZedParserTest extends FunSuite {
  def printResult[E](r: Either[E, JSONGrammar]): Unit = r.fold(println, println)
  def parser: ZedParser[JSONGrammar] = JSONGrammar.jsonParser(ZedParser)

  test("fail unclosed array") {
    val actual = parser.run("[,1,2")
    assertEquals(actual.isLeft, true)
  }
  test("empty array") {
    val actual = parser.run("[]")
    val expected = Right(JArray(Vector()))
    assertEquals(actual, expected)
  }

  test("array with numbers") {
    val actual = parser.run("[1, 2, 3]")
    val expected =
      Right(JArray(Vector(JNumber(1.0), JNumber(2.0), JNumber(3.0))))
    assertEquals(actual, expected)
  }

  test("the whole object") {
    val jsonTxt =
      """
        |{
        |  "Company name" : "Microsoft Corporation",
        |  "Ticker"  : "MSFT",
        |  "Active"  : true,
        |  "Price"   : 30.66,
        |  "Shares outstanding" : 8.38e9,
        |  "Related companies" : [ "HPQ", "IBM", "YHOO", "DELL", "GOOG" ]
        |}
        |""".stripMargin
    val actual = parser.run(jsonTxt)
    assertEquals(actual.isRight, true)
  }
  test("very long array") {
    val actual = parser.run("[" + (1 to 10000).mkString(",") + "]")
    assertEquals(actual.isRight, true)
  }
}
