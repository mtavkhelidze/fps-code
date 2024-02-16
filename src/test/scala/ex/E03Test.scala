package ge.zgharbi.study.fps
package ex

import ch.c03.Tree

import munit.FunSuite

class E03Test extends FunSuite {
  import Tree.*

  test("03.25 Tree[Int]#maximum") {
    import Tree.*
    val tree: Tree[Int] = Branch(
      Branch(
        Leaf(1),
        Branch(
          Leaf(2),
          Leaf(11),
        ),
      ),
      Leaf(4),
    )
    assertEquals(11, tree.maximum)
  }
}
