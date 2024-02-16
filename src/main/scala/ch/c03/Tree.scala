package ge.zgharbi.study.fps
package ch.c03

import scala.annotation.tailrec

enum Tree[+A] {
  case Leaf(value: A)
  case Branch(left: Tree[A], right: Tree[A])

  def map[B](f: A => B): Tree[B] = this match
    case Leaf(value)         => Leaf(f(value))
    case Branch(left, right) => Branch(left.map(f), right.map(f))
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

    // 03.26
    // Write a function, depth, that returns the maximum path length from the root of a tree to any leaf.
    def depth[A](needle: Leaf[A]): Int =
      t match
        case Leaf(x) if x == needle.value => 1
        case Branch(l, r) => 1 + l.depth(needle) max r.depth(needle)
        case _            => 0
}
