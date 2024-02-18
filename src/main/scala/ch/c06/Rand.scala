package ge.zgharbi.study.fps
package ch.c06

opaque type Rand[+A] = RNG => (A, RNG)

object Rand {
  val int: Rand[Int] = _.nextInt
}
