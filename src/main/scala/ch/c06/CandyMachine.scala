package ge.zgharbi.study.fps
package ch.c06

object CandyMachine {
  val update = (i: Input) =>
    (s: Machine) =>
      (i, s) match {
        case (Input.Coin, Machine(true, candies, coins)) if candies > 0 =>
          Machine(locked = false, candies = candies, coins = coins + 1)
        case (Input.Turn, Machine(false, candies, coins)) if candies > 0 =>
          Machine(locked = true, candies = candies - 1, coins = coins)
        case _ => s
      }

  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] =
    State
      .traverse(inputs)((i: Input) =>
        State.modify(CandyMachine.update.apply(i)),
      )
      .flatMap((_: List[Unit]) =>
        State
          .get[Machine]
          .map((s: Machine) => (s.coins, s.candies)),
      )

  case class Machine(locked: Boolean, candies: Int, coins: Int)

  enum Input {
    case Coin
    case Turn
  }
}
