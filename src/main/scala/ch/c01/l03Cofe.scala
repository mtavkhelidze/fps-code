package ge.zgharbi.study.fps
package ch.c01

import scala.annotation.targetName

case class CreditCard(num: String)
case class Coffee(price: Double)

case class Charge(cc: CreditCard, amount: Double)

object Charge {
  extension (c: Charge)
    @targetName("combine")
    def ++(other: Charge): Charge =
      if c.cc == other.cc then Charge(c.cc, c.amount + other.amount)
      else
        throw new Exception(
          "Cannot combine charges from different credit cards",
        )
}

class Cafe {
  def buyManyCoffee(cc: CreditCard, nCups: Int): (List[Coffee], Charge) = {
    val purchases: List[(Coffee, Charge)] =
      List.fill(nCups)(buyOneCoffee(cc))
    val (ccs, charges) = purchases.unzip
    val combined = charges.reduce(_ ++ _)
    (ccs, combined)
  }

  def buyOneCoffee(cc: CreditCard): (Coffee, Charge) =
    (Coffee(5.0), Charge(cc, 5.0))
}
