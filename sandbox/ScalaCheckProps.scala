//> using dep "org.scalacheck::scalacheck:1.17.0"

import org.scalacheck.{Gen, Prop}

val li = Gen.listOf(Gen.choose(0, 100))
val prop = Prop.forAll(li)(ns => ns.reverse.reverse == ns) &&
  Prop.forAll(li)(ns => ns.headOption == ns.reverse.lastOption)

val failing = Prop.forAll(li)(ns => ns.reverse == ns)

@main def main(): Unit = {
  prop.check()
  failing.check()
}
