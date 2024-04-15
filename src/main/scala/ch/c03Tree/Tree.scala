package ge.zgharbi.study.fps
package ch.c03Tree

enum Tree[+A] {
  case Leaf(value: A)
  case Branch(left: Tree[A], right: Tree[A])

  // 3.27 (modified to use fold)
  def map[B](f: A => B): Tree[B] =
    fold(x => Leaf(f(x)))((l, r) => Branch(l, r))

  // 3.28
  def fold[B](op: A => B)(combine: (B, B) => B): B = this match
    case Leaf(value) => op(value)
    case Branch(left, right) =>
      combine(left.fold(op)(combine), right.fold(op)(combine))
}

object Tree {
  // 03.25 (modified to use fold)
  // Write a function, maximum, that returns
  // the maximum element in a Tree[Int].
  extension (t: Tree[Int])
    def maximum: Int = t.fold(x => x)(_ max _)

    // 03.26 (modified to use fold)
    // Write a function, depth, that returns the maximum path length from the root of a tree to any leaf.
    def depth[A](needle: Leaf[A]): Int =
      t.fold(_ => 0)((dl, dr) => 1 + dl max dr)
}
