package ge.zgharbi.study.fps
package ex

import ch.c04.Option

object E04 {
  import Option.*

  // 04.02
  def variance(xs: Seq[Double]): Option[Double] =
    mean(xs).flatMap(m => mean(xs.map(x => math.pow(x - m, 2))))

  def mean(xs: Seq[Double]): Option[Double] =
    if xs.isEmpty then None
    else Some(xs.sum / xs.length)
}
