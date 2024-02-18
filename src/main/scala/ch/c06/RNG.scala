package ge.zgharbi.study.fps
package ch.c06

object LCG {
  final val MODULUS = 0xfffffffffffffff1L
  final val MULTIPLIER = 0x426ae571583e6443L
  final val INCREMENT = 0x7855fc643f22fc01L
}
trait RNG:
  def nextInt: (Int, RNG)

object RNG {
  case class SimpleRNG(seed: Long) extends RNG:
    import LCG.*

    override def nextInt: (Int, RNG) =
      val nSeed = compute(seed)
      val next = SimpleRNG(nSeed)
      val n = (nSeed >>> 16).toInt
      (n, next)

    inline def compute(s: Long): Long = (MULTIPLIER * s + INCREMENT) & MODULUS

  extension (rng: RNG)
    def nonNegativeInt: (Int, RNG) =
      val (n, r) = rng.nextInt
      (if n > 0 then n else -(n + 1), r)

    def double: (Double, RNG) =
      val (n, r) = rng.nextInt
      (n / Int.MaxValue.toDouble + 1, r)
}
