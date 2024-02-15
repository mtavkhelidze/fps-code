## Functional Programming in Scala

##### _Code Companion & Notes for Second Edition (Scala 3)_

<hr/>

### Notes

#### Chapter 1: What is Functional Programming

- Functional programming is the construction of programs using
  only pure functions—functions that do not have side effects.
- A side effect is something a function
  does aside from simply returning a result.

  If (_impure_) function with side-effects is memoised, it _will not_
  perform its side-effect on consecutive calls as in the following code.

  ```scala
  
  import scala.collection.mutable
  
  val memoizedAddOne = Memoized.of(impureAddOne)
  
  def impureAddOne(n: Int): Int = {
    println(s"Got $n")
    n + 1
  }
  
  class Memoized[A, B] private (f: A => B) {
    private var storage: mutable.Map[A, B] = mutable.Map.empty
  
    def apply(a: A): B = storage.get(a) match {
      case Some(value) => value
      case None =>
        val b = f(a)
        storage.addOne(a -> b)
        b
    }
  }
  
  object Memoized {
    def of[A, B](f: A => B): Memoized[A, B] = new Memoized(f)
  }
  
  memoizedAddOne(1) // prints "Got 1"
  memoizedAddOne(1) // doesn't print anything 
  ```

- Examples of side effects include modifying a field on an object,
  throwing an exception, and accessing the network or file system.
- Functional programming constrains the way we write
  programs but does not limit our expressive power.
- Side effects limit our ability to understand,
  compose, test, and refactor parts of our programs.
- Moving side effects to the outer edges of our program results in a pure core
  and thin outer layer, which handles effects and results in better testability.
- Referential transparency defines whether an
  expression is pure or contains side effects.
- The substitution model provides a way to test
  whether an expression is referentially transparent.
- Functional programming enables local reasoning and allows
  the embedding of smaller programs within larger programs. »
