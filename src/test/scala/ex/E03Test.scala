package ge.zgharbi.study.fps
package ex

import ch.c03.Tree

import munit.FunSuite

class E03Test extends FunSuite {
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

  test("03.25 Tree[Int]#maximum") {
    assertEquals(11, tree.maximum)
  }

  test("03.26 Tree[Int]#depth") {
    assertEquals(tree.depth(Leaf(11)), 2)
  }

  test("03.27 Tree[Int]#map") {
      println(tree.map(x => s"value: ${x}"))
  }
}
