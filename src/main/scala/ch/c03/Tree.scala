package ge.zgharbi.study.fps
package ch.c03

enum Tree[+A] {
  case Leaf(value: A)
  case Branch(left: Tree[A], right: Tree[A])
}

object Tree {
  // 03.25
  // Write a function, maximum, that returns
  // the maximum element in a Tree[Int].
  extension (t: Tree[Int])
    def maximum: Int =
      t match
        case Leaf(value)         => value
        case Branch(left, right) => left.maximum max right.maximum
}
