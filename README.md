Functional Programming in Scala
---
_[Code Companion](https://github.com/mtavkhelidze/fps-code) & Notes for [Second Edition](https://www.amazon.com/Functional-Programming-Second-Michael-Pilquist/dp/1617299588) using Scala 3_

[Part One: Introduction to functional programming](#part-one-introduction-to-functional-programming)<br/>
&nbsp;&nbsp;[Chapter 1: What is Functional Programming](#chapter-1-what-is-functional-programming)<br/>
&nbsp;&nbsp;[Chapter 2: Getting started with FP in Scala](#chapter-2-getting-started-with-fp-in-scala)<br/>
&nbsp;&nbsp;[Chapter 3: Functional data structures](#chapter-3-functional-data-structures)<br/>
&nbsp;&nbsp;[Chapter 4: Handling errors without exceptions](#chapter-4-handling-errors-without-exceptions)<br/>
&nbsp;&nbsp;[Chapter 5: Strictness and laziness](#chapter-5-strictness-and-laziness)<br/>
&nbsp;&nbsp;[Chapter 6: Purely functional state](#chapter-6-purely-functional-state)<br/>
[Part Two: Functional design and combinator libraries](#part-two-functional-design-and-combinator-libraries)

### Part One: Introduction to functional programming

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

**Quotes**

> Between `map`, `lift`, `sequence`, `traverse`,
`map2`, `map3`, and so on, you should
> never have to modify any existing functions to work with optional values.

- Throwing exceptions is a side effect because
  doing so breaks referential transparency.
- Throwing exceptions inhibits local reasoning because program
  meaning changes depending on which try block a throw is nested in.
- Exceptions are not type safe; the potential for an error
  occurring is not communicated in the type of the function,
  leading to unhandled exceptions becoming runtime errors.
- Instead of exceptions, we can model errors as values.
- Rather than modeling error values as return codes, we
  use various ADTs that describe success and failure.
- The Option type has two data constructors, Some(a)
  and None, which are used to model a successful result
  and an error. No details are provided about the error.
- The Either type has two data constructors, Left(e) and Right(a), which
  are used to model an error and a successful result. The Either type
  is similar to Option, except it provides details about the error.
- The Try type is like Either, except errors are represented as
  Throwable values instead of arbitrary types. By constraining
  errors to be subtypes of Throwable, the Try type is able to provide
  various convenience operations for code that throws exceptions.
- The Validated type is like Either, except errors are
  accumulated when combining multiple failed computations.
- Higher-order functions, like map and flatMap, let us work with
  potentially failed computations without explicitly handling an
  error from every function call. These higher-order functions
  are defined for each of the various error-handling data types.

#### Chapter 5: Strictness and laziness

- Non-strictness is a useful technique that allows
  separation of concerns and improved modularity.
- A function is described as non-strict when it does not evaluate one or more of
  its arguments. In contrast, a strict function always evaluates its arguments.
- The short circuiting `&&` and `||` operators are examples
  of non-strict functions; each avoids evaluation of its second
  argument, depending on the value of the first argument.
- In Scala, non-strict functions are written using by-name parameters,
  which are indicated by a `=>` in front of the parameter type.
- An unevaluated expression is referred to as a _thunk_, and we
  can force a thunk to evaluate the expression to a result.
- `LazyList` allows the definition of infinitely long sequences,
  and various operations on `LazyList`, like `take`, `foldRight`, and
  `exists`, allow partial evaluation of those infinite sequences.
- `LazyList` is similar to `List`, except the head and tail of `Cons`
  are evaluated lazily instead of strictly, like in `List.Cons`.
- Memoization is the process of caching the
  result of a computation upon first use.
- Smart constructors are functions that create an instance of a
  data type and provide some additional validation or provide a
  slightly different type signature than the real data constructors.
- Separating the description of a computation from its
  evaluation provides opportunities for reuse and efficiency.
- The `foldRight` function on `LazyList` supports early termination
  and is subsequently safe for use with infinite lazy lists.
- The unfold function, which generates a `LazyList` from a seed and a
  function, is an example of a _corecursive_ function. Corecursive functions
  produce data and continue to run as long as they are productive.

#### Chapter 6: Purely functional state

**Quotes**

> Don’t be afraid of using the <u>_simpler case class
encoding_</u> and only refactoring o opaque types if
> allocation cost proves to be a bottleneck in your program.

> <u>_Imperative and functional programming absolutely aren’t opposites_</u>.
> Remember that functional programming is simply programming without
> side effects. Imperative programming is about programming with
> statements that modify some program state, and as we’ve seen,
> it’s entirely reasonable to maintain state without side effects.

- The `scala.util.Random` type provides generation of pseudo-random
  numbers but performs generation as a side effect.
- Pseudo-random number generation can be modeled as a pure function
  from an input seed to an output seed and a generated value.
- Making stateful APIs pure by having the API compute the next state
  from the current state rather than actually mutating anything is a
  general technique not limited to pseudo-random number generation.
- When the functional approach feels awkward or tedious,
  look for common patterns that can be factored out.
- The `Rand[A]` type is an alias for a function `Rng => (A, Rng)`.
  There are a variety of functions that create and transform
  `Rand` values, like `unit`, `map`, `map2`, `flatMap`, and `sequence`.
- Opaque types behave like type aliases in their defining scope but
  behave like unrelated types outside their defining scope. An opaque
  type encapsulates the relationship with the representation type,
  allowing the definition of new types without runtime overhead.
- Extension methods can be used to add methods to opaque types.
- Case classes can be used instead of opaque types, but they
  come with the runtime cost of wrapping the underlying value.
- The `State[S, A]` type is an opaque alias for a function `S => (A, S)`.
- State supports the same operations as Rand — `unit`, `map`,
  `map2`, `flatMap`, and `sequence` — since none of these
  operations had any dependency on `Rng` being the state type.
- The `State` data type simplifies working with stateful APIs by removing the
  need to manually thread input and output states throughout computations.
- State computations can be built with for-comprehensions,
  which result in imperative-looking code.


### Part Two: Functional design and combinator libraries
