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
  type Rand[+A] = RNG => (A, RNG)

  val int: Rand[Int] = _.nextInt

  val nonNegativeEven: Rand[Int] =
    map(nonNegativeInt)(i => i - (i % 2))

  val double: Rand[Double] =
    map(nonNegativeInt)(_ / (Int.MaxValue.toDouble - 1))

  def randIntDouble: Rand[(Int, Double)] =
    int both double

  def randDoubleInt: Rand[(Double, Int)] =
    double both int

  def ints(cnt: Int): Rand[List[Int]] =
    sequence(List.fill(cnt)(int))

  def sequence[A](rs: List[Rand[A]]): Rand[List[A]] =
    rs.foldLeft(unit(Nil: List[A]))((acc, r) => r.map2(acc)(_ :: _))
      .map(_.reverse)

  def unit[A](a: A): Rand[A] = rng => (a, rng)

  extension [A](self: Rand[A])
    def flatMap[B](f: A => Rand[B]): Rand[B] =
      rng =>
        val (a, r1) = self(rng)
        f(a)(rng)

    def map[B](f: A => B): Rand[B] = rng =>
      val (a, r1) = self(rng)
      (f(a), r1)

    def map2[B, C](other: Rand[B])(f: (A, B) => C): Rand[C] =
      rng => {
        lazy val (a, r1) = self(rng)
        lazy val (b, r2) = other(r1)
        (f(a, b), r2)
      }

    def both[B](other: Rand[B]): Rand[(A, B)] = self.map2(other)((_, _))

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

    def intDouble: ((Int, Double), RNG) =
      val (n, r1) = rng.nextInt
      val (d, r2) = r1.double
      ((n, d), r2)

    def doubleInt: ((Double, Int), RNG) =
      val ((n, d), r1) = rng.intDouble
      ((d, n), r1)

    def double3: ((Double, Double, Double), RNG) =
      val (d1, r1) = rng.double
      val (d2, r2) = r1.double
      val (d3, r3) = r2.double
      ((d1, d2, d3), r3)

    def ints(cnt: Int): (List[Int], RNG) =
      (1 to cnt).foldLeft(List[Int](), rng) { case ((acc, r), _) =>
        val (n, r1) = r.nextInt
        (n :: acc, r1)
      }
}
