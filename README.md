---
<style>
  .small-text {
    font-size: 80%;
    padding: 0;
    margin: 0;
    line-height: 1;
  }
</style>
---

Functional Programming in Scala
---
Code Companion & Notes for Second Edition (Scala 3)

<sub>[FPScala Github](https://github.com/fpinscala/fpinscala) | [This GitHub](https://github.com/mtavkhelidze/fps-code)</sub>

<span class="small-text">[Chapter 1: What is Functional Programming](#chapter-1-what-is-functional-programming)
</span><br/>
<span class="small-text">[Chapter 2: Getting started with FP in Scala](#chapter-2-getting-started-with-fp-in-scala)
</span><br/>
<span class="small-text">[Chapter 3: Functional data structures](#chapter-3-functional-data-structures)
</span><br/>
<span class="small-text">[Chapter 4: Handling errors without exceptions](#chapter-4-handling-errors-without-exceptions)
</span></br>

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
  the embedding of smaller programs within larger programs.

#### Chapter 2: Getting started with FP in Scala

- Scala is a mixed paradigm language, blending concepts from
  both objected-oriented programming and functional programming.
- The object keyword creates a new singleton type. Objects contain
  members such as method definitions, values, and additional objects.
- Scala supports top-level definitions, but objects
  provide a convenient way to group related definitions.
- Methods are defined with the def keyword.
- The definition of a method can be a single
  expression or a block with multiple statements.
- Method definitions can contain local definitions, such as nested methods.
- The result of a method is the value of its right-hand
  side. There’s no need for an explicit return statement.
- The `@main` annotation defines an entry point of a program.
- The Unit type serves a similar purpose to void in C and Java.
  There’s one value of the Unit type, which is written as `()`.
- The import keyword allows us to reference the members of a namespace (that
  is, an object or package) without writing out their fully qualified names.
- Recursive functions allow the expression of looping without mutation.
  All loops can be rewritten as recursive functions and vice versa.
- Tail-recursive functions are recursive functions that limit
  recursive calls to the tail position—that is, the result of the
  recursive call is returned directly, with no further manipulation.
- Tail recursion ensures the stack does not grow with each recursive call.
- Higher-order functions are functions that
  take one or more functions as parameters.
- Polymorphic functions, also known as generic functions,
  are functions that use one or more type parameters in
  their signature, allowing them to operate on many types.
- A function with no type parameters is monomorphic.
- Polymorphic functions allow us to remove extraneous detail, resulting
  in definitions that are easier to read and write and are reusable.
- Polymorphic functions constrain the possible implementations of a function
  signature. Sometimes there is only a single possible implementation.
- Determining the implementation of a polymorphic function from its signature
  is known as following types to implementations or type-driven development.

#### Chapter 3: Functional data structures

- Functional data structures are immutable and
  are operated on using only pure functions.
- Algebraic data types (ADTs) are defined via a set of data constructors.
- ADTs are expressed in Scala with enumerations or sealed trait hierarchies.
- Enumerations may take type parameters, and each
  data constructor may take zero or more arguments.
- Singly linked lists are modeled as an ADT with two data
  constructors: `Cons(head: A, tail: List[A])` and `Nil`.
- Companion objects are objects with the same name as
  a data type. Companion objects have access to the
  private and protected members of the companion type.
- Pattern matching lets us destructure an algebraic data type, allowing
  us to inspect the values used to construct the algebraic data type.
- Pattern matches can be defined to be exhaustive, meaning one of the cases
  is always matched. A non-exhaustive pattern match may throw a MatchError.
  The compiler often warns when defining a non-exhaustive pattern match.
- Purely functional data structures use persistence, also
  known as structural sharing, to avoid unnecessary copying.
- With singly linked lists, some operations can be implemented with no copying,
  like prepending an element to the front of a list. Other operations require
  copying the entire structure, like appending an element to the end of a list.
- Many algorithms can be implemented with recursion on the structure
  of an algebraic data type, with a base case associated with one data
  constructor and recursive cases associated with other data constructors.
- `foldRight` and `foldLeft` allow us to compute a
  single result by visiting all the values of a list.
- `map`, `filter`, and `flatMap` are higher-order
  functions that compute a new list from an input list.
- Extension methods allow object-oriented style methods to be defined for
  a type in an ad hoc fashion separate from the definition of the type.

#### Chapter 4: Handling errors without exceptions

#### Quotes

    Between map, lift, sequence, traverse, map2, map3, and so on, you should
    never have to modify any existing functions to work with optional values. »
