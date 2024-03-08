package ge.zgharbi.study.fps
package ex

import munit.FunSuite

class E09ParserTest extends FunSuite {

  import ch.ch09.Parser
  import ch.ch09.Parser.*
  test("Parser#run") {
    char('a').run("a")
  }

}
