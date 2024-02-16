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
    assertEquals(tree.maximum, 11)
  }

  test("03.26 Tree#depth") {
    assertEquals(tree.depth(Leaf(11)), 2)
  }

  test("03.27 Tree#map") {
    assertEquals(tree.map(x => x * 2).maximum, 22)
  }
}
