Functional Programming in Scala
---
_[Code Companion](https://github.com/mtavkhelidze/fps-code) & Notes
for [Second Edition](https://www.amazon.com/Functional-Programming-Second-Michael-Pilquist/dp/1617299588)
using Scala 3_

<br/>[Part One: Introduction to functional programming](#part-one-introduction-to-functional-programming)<br/>
&nbsp;&nbsp;[Chapter 01: What is Functional Programming](#chapter-1-what-is-functional-programming)<br/>
&nbsp;&nbsp;[Chapter 02: Getting started with FP in Scala](#chapter-2-getting-started-with-fp-in-scala)<br/>
&nbsp;&nbsp;[Chapter 03: Functional data structures](#chapter-3-functional-data-structures)<br/>
&nbsp;&nbsp;[Chapter 04: Handling errors without exceptions](#chapter-4-handling-errors-without-exceptions)<br/>
&nbsp;&nbsp;[Chapter 05: Strictness and laziness](#chapter-5-strictness-and-laziness)<br/>
&nbsp;&nbsp;[Chapter 06: Purely functional state](#chapter-6-purely-functional-state)<br/>
<br/>[Part Two: Functional design and combinator libraries](#part-two-functional-design-and-combinator-libraries)<br/>
&nbsp;&nbsp;[Chapter 07: Purely functional parallelism](#chapter-7-purely-functional-parallelism)<br/>
&nbsp;&nbsp;[Chapter 08: Property-based testing](#chapter-8-property-based-testing)<br/>
&nbsp;&nbsp;[Chapter 09: Parser combinators](#chapter-9-parser-combinators)</br>
<br/>[Part 3. Common structures in functional design](#part-3-common-structures-in-functional-design)<br/>
&nbsp;&nbsp;[Chapter 10: Monoids](#chapter-10-monoids)<br/>
&nbsp;&nbsp;[Chapter 11: Monads](#chapter-11-monads)<br/>
&nbsp;&nbsp;[Chapter 12: Applicative and traversable functors](#chapter-12-applicative-and-traversable-functors)<br/>

### Part One: Introduction to functional programming

#### Chapter 01: What is Functional Programming

- Functional programming is the construction of programs using only pure
  functions—functions that do not have side effects.
- A side effect is something a function does aside from simply returning a
  result.

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

- Examples of side effects include modifying a field on an object, throwing an
  exception, and accessing the network or file system.
- Functional programming constrains the way we write programs but does not limit
  our expressive power.
- Side effects limit our ability to understand, compose, test, and refactor
  parts of our programs.
- Moving side effects to the outer edges of our program results in a pure core
  and thin outer layer, which handles effects and results in better testability.
- Referential transparency defines whether an expression is pure or contains
  side effects.
- The substitution model provides a way to test whether an expression is
  referentially transparent.
- Functional programming enables local reasoning and allows the embedding of
  smaller programs within larger programs.

#### Chapter 02: Getting started with FP in Scala

- Scala is a mixed paradigm language, blending concepts from both
  objected-oriented programming and functional programming.
- The object keyword creates a new singleton type. Objects contain members such
  as method definitions, values, and additional objects.
- Scala supports top-level definitions, but objects provide a convenient way to
  group related definitions.
- Methods are defined with the def keyword.
- The definition of a method can be a single expression or a block with multiple
  statements.
- Method definitions can contain local definitions, such as nested methods.
- The result of a method is the value of its right-hand side. There’s no need
  for an explicit return statement.
- The `@main` annotation defines an entry point of a program.
- The Unit type serves a similar purpose to void in C and Java. There’s one
  value of the Unit type, which is written as `()`.
- The import keyword allows us to reference the members of a namespace (that is,
  an object or package) without writing out their fully qualified names.
- Recursive functions allow the expression of looping without mutation. All
  loops can be rewritten as recursive functions and vice versa.
- Tail-recursive functions are recursive functions that limit recursive calls to
  the tail position—that is, the result of the recursive call is returned
  directly, with no further manipulation.
- Tail recursion ensures the stack does not grow with each recursive call.
- Higher-order functions are functions that take one or more functions as
  parameters.
- Polymorphic functions, also known as generic functions, are functions that use
  one or more type parameters in their signature, allowing them to operate on
  many types.
- A function with no type parameters is monomorphic.
- Polymorphic functions allow us to remove extraneous detail, resulting in
  definitions that are easier to read and write and are reusable.
- Polymorphic functions constrain the possible implementations of a function
  signature. Sometimes there is only a single possible implementation.
- Determining the implementation of a polymorphic function from its signature is
  known as following types to implementations or type-driven development.

#### Chapter 03: Functional data structures

- Functional data structures are immutable and are operated on using only pure
  functions.
- Algebraic data types (ADTs) are defined via a set of data constructors.
- ADTs are expressed in Scala with enumerations or sealed trait hierarchies.
- Enumerations may take type parameters, and each data constructor may take zero
  or more arguments.
- Singly linked lists are modeled as an ADT with two data
  constructors: `Cons(head: A, tail: List[A])` and `Nil`.
- Companion objects are objects with the same name as a data type. Companion
  objects have access to the private and protected members of the companion
  type.
- Pattern matching lets us destructure an algebraic data type, allowing us to
  inspect the values used to construct the algebraic data type.
- Pattern matches can be defined to be exhaustive, meaning one of the cases is
  always matched. A non-exhaustive pattern match may throw a MatchError. The
  compiler often warns when defining a non-exhaustive pattern match.
- Purely functional data structures use persistence, also known as structural
  sharing, to avoid unnecessary copying.
- With singly linked lists, some operations can be implemented with no copying,
  like prepending an element to the front of a list. Other operations require
  copying the entire structure, like appending an element to the end of a list.
- Many algorithms can be implemented with recursion on the structure of an
  algebraic data type, with a base case associated with one data constructor and
  recursive cases associated with other data constructors.
- `foldRight` and `foldLeft` allow us to compute a single result by visiting all
  the values of a list.
- `map`, `filter`, and `flatMap` are higher-order functions that compute a new
  list from an input list.
- Extension methods allow object-oriented style methods to be defined for a type
  in an ad hoc fashion separate from the definition of the type.

#### Chapter 04: Handling errors without exceptions

**Quotes**

> Between `map`, `lift`, `sequence`, `traverse`,
`map2`, `map3`, and so on, you should
> never have to modify any existing functions to work with optional values.

- Throwing exceptions is a side effect because doing so breaks referential
  transparency.
- Throwing exceptions inhibits local reasoning because program meaning changes
  depending on which try block a throw is nested in.
- Exceptions are not type safe; the potential for an error occurring is not
  communicated in the type of the function, leading to unhandled exceptions
  becoming runtime errors.
- Instead of exceptions, we can model errors as values.
- Rather than modeling error values as return codes, we use various ADTs that
  describe success and failure.
- The Option type has two data constructors, Some(a)
  and None, which are used to model a successful result and an error. No details
  are provided about the error.
- The Either type has two data constructors, Left(e) and Right(a), which are
  used to model an error and a successful result. The Either type is similar to
  Option, except it provides details about the error.
- The Try type is like Either, except errors are represented as Throwable values
  instead of arbitrary types. By constraining errors to be subtypes of
  Throwable, the Try type is able to provide various convenience operations for
  code that throws exceptions.
- The Validated type is like Either, except errors are accumulated when
  combining multiple failed computations.
- Higher-order functions, like map and flatMap, let us work with potentially
  failed computations without explicitly handling an error from every function
  call. These higher-order functions are defined for each of the various
  error-handling data types.

#### Chapter 05: Strictness and laziness

- Non-strictness is a useful technique that allows separation of concerns and
  improved modularity.
- A function is described as non-strict when it does not evaluate one or more of
  its arguments. In contrast, a strict function always evaluates its arguments.
- The short circuiting `&&` and `||` operators are examples of non-strict
  functions; each avoids evaluation of its second argument, depending on the
  value of the first argument.
- In Scala, non-strict functions are written using by-name parameters, which are
  indicated by a `=>` in front of the parameter type.
- An unevaluated expression is referred to as a _thunk_, and we can force a
  thunk to evaluate the expression to a result.
- `LazyList` allows the definition of infinitely long sequences, and various
  operations on `LazyList`, like `take`, `foldRight`, and
  `exists`, allow partial evaluation of those infinite sequences.
- `LazyList` is similar to `List`, except the head and tail of `Cons`
  are evaluated lazily instead of strictly, like in `List.Cons`.
- Memoization is the process of caching the result of a computation upon first
  use.
- Smart constructors are functions that create an instance of a data type and
  provide some additional validation or provide a slightly different type
  signature than the real data constructors.
- Separating the description of a computation from its evaluation provides
  opportunities for reuse and efficiency.
- The `foldRight` function on `LazyList` supports early termination and is
  subsequently safe for use with infinite lazy lists.
- The unfold function, which generates a `LazyList` from a seed and a function,
  is an example of a _corecursive_ function. Corecursive functions produce data
  and continue to run as long as they are productive.

#### Chapter 06: Purely functional state

**Quotes**

> Don’t be afraid of using the <u>_simpler case class encoding_</u> and only
> refactoring o opaque types if
> allocation cost proves to be a bottleneck in your program.

> <u>_Imperative and functional programming absolutely aren’t opposites_</u>.
> Remember that functional programming is simply programming without
> side effects. Imperative programming is about programming with
> statements that modify some program state, and as we’ve seen,
> it’s entirely reasonable to maintain state without side effects.

- The `scala.util.Random` type provides generation of pseudo-random numbers but
  performs generation as a side effect.
- Pseudo-random number generation can be modeled as a pure function from an
  input seed to an output seed and a generated value.
- Making stateful APIs pure by having the API compute the next state from the
  current state rather than actually mutating anything is a general technique
  not limited to pseudo-random number generation.
- When the functional approach feels awkward or tedious, look for common
  patterns that can be factored out.
- The `Rand[A]` type is an alias for a function `Rng => (A, Rng)`. There are a
  variety of functions that create and transform
  `Rand` values, like `unit`, `map`, `map2`, `flatMap`, and `sequence`.
- Opaque types behave like type aliases in their defining scope but behave like
  unrelated types outside their defining scope. An opaque type encapsulates the
  relationship with the representation type, allowing the definition of new
  types without runtime overhead.
- Extension methods can be used to add methods to opaque types.
- Case classes can be used instead of opaque types, but they come with the
  runtime cost of wrapping the underlying value.
- The `State[S, A]` type is an opaque alias for a function `S => (A, S)`.
- State supports the same operations as Rand — `unit`, `map`,
  `map2`, `flatMap`, and `sequence` — since none of these operations had any
  dependency on `Rng` being the state type.
- The `State` data type simplifies working with stateful APIs by removing the
  need to manually thread input and output states throughout computations.
- State computations can be built with for-comprehensions, which result in
  imperative-looking code.

### Part Two: Functional design and combinator libraries

#### Chapter 07: Purely functional parallelism

* No existing library is beyond reexamination. Most libraries contain arbitrary
  design choices. Experimenting with building alternative libraries may result
  in discovering new things about the problem space.
* Simple examples let us focus on the essence of the problem domain instead of
  getting lost in incidental detail.
* Parallel and asynchronous computations can be modeled in a purely functional
  way.
* Building a description of a computation along with a separate interpreter that
  runs the computations allows computations to be treated as values, which can
  then be combined with other computations.
* An effective technique for API design is conjuring types and implementations,
  trying to implement those types and implementations, adjusting, and iterating.
* The `Par[A]` type describes a computation that may evaluate some or all of the
  computation on multiple threads.
* Par values can be transformed and combined with many familiar operations, such
  as `map`, `flatMap`, and `sequence`.
* Treating an API as an algebra and defining laws that constrain implementations
  are both valuable design tools and an effective testing technique.
* Partitioning an API into a minimal set of primitive functions and a set of
  combinator functions promotes reuse and understanding.
* An _actor_ is a non-blocking concurrency primitive based on message passing.
  Actors are not purely functional but can be used to implement purely
  functional APIs, as demonstrated with the implementation of `map2` for the
  non-blocking `Par`.

#### Chapter 08: Property-based testing

* Properties of our APIs can be automatically tested and, in some cases, proven
  using property-based testing.
* A property can be expressed as a function that takes arbitrary input values
  and asserts a desired outcome based on those inputs.
* In the library developed in this chapter, the `Prop` type is used to model
  these properties.
* Properties defined in this way assert various invariants about the
  functionality being tested—things that should be true for all possible input
  values. An example of such a property is that reversing a list and then
  summing the elements should be the same as summing the original list.
* Defining properties for arbitrary input values requires a way of generating
  such values.
* In the library developed in this chapter, the `Gen` and `SGen` types are used
  for arbitrary value generation.
* Test case minimization is the ability of a property-based testing library to
  find the smallest input values that fail a property.
* Exhaustive test case generation is the ability to generate all possible inputs
  to a property. This is only possible for finite domains and only practically
  possible when the size of the domain is small.
* If every possible input to a property is tested and all pass, then the
  property is proved true. If instead, the property simply does not fail for any
  of the generated inputs, then the property is passed. There might still be
  some input that wasn’t generated but fails the property.
* Combinators like `map` and `flatMap` continue to appear in data types we
  create, and their implementations satisfy the same laws.

#### Chapter 09: Parser combinators

* A parser converts a sequence of unstructured data into a structured
  representation.
* A _**parser combinator**_ library allows the construction of a parser by
  combining simple primitive parsers and generic combinators.
* In contrast, a _**parser generator**_ library constructs a parser based on the
  specification of a grammar.
* Algebraic design is the process in which an interface and associated laws are
  designed first and then used to guide the choice of data type representations.
* Judicious use of infix operators, either defined with symbols as in `|` or
  with the `infix` keyword as in
  `or`, can make combinator libraries easier to use.
* The `many` combinator creates a parser that _parses zero or more_
  repetitions of the input parser and returnsa list of parsed values.
* The `many1` combinator is like many but _parses one or more_ repetitions.
* The `product` combinator (or `**` operator) creates a parser from two input
  parsers, which runs the first parser and, if successful, runs the second
  parser on the remaining input. The resulting value of each input parser is
  returned in a tuple.
* The `map`, `map2`, and `flatMap` operations are useful for building composite
  parsers. In particular, `flatMap` allows the creation of context-sensitive
  parsers.
* The `label` and `scope` combinators allow better error messages to be
  generated with parsing fails.
* APIs can be designed by choosing primitive operations, building combinators,
  and deciding how those operations and combinators should interact.
* API design is an iterative process, where interactions amongst operations,
  sample usages, and implementation difficulties all contribute to the process.

### Part 3. Common structures in functional design

#### Chapter 10: Monoids

> There is a slight terminology mismatch between programmers and mathematicians
> when they talk about a type _being a monoid_ versus having _a monoid
instance_.
> As a programmer, it’s tempting to think of the instance of type `Monoid[A]` as
> being a monoid, but that’s not accurate terminology. The monoid is actually
> both
> things—the type together with the instance satisfying the laws. It’s more
> accurate to say that type `A` forms a monoid under the operations defined by
> the
> `Monoid[A]` instance. Less precisely, we might say type `A` is a monoid or
> even
> type
> `A` is monoidal. In any case, the `Monoid[A]` instance is simply evidence of
> this fact.
>
> This is much the same as saying that the page or screen you’re reading forms a
> rectangle or is rectangular. It’s less accurate to say it is a rectangle (
> although that still makes sense), but to say that it has a rectangle would be
> strange.

* A monoid is a purely algebraic structure consisting of an associative binary
  operation and an identity element for that operation.
* Associativity lets us move the parentheses around in an expression without
  changing the result.
* Example monoids include string concatenation with the empty string as the
  identity, integer addition with 0 as the identity, integer multiplication with
  1 as the identity, Boolean and with true as the identity, Boolean or with
  false as the identity, and list concatenation with `Nil` as the identity.
* Monoids can be modeled as `traits` with `combine` and `empty` operations.
* Monoids allow us to write useful, generic functions for a wide variety of data
  types.
* The `combineAll` function folds the elements of a list into a single value
  using a monoid.
* The `foldMap` function maps each element of a list to a new type and then
  combines the mapped values with a monoid instance.
* `foldMap` can be implemented with `foldLeft` or `foldRight`, and
  both `foldLeft` and
  `foldRight` can be implemented with `foldMap`.
* The various monoids encountered in this chapter had nothing in common besides
  their monoidal structure.
* Typeclasses allow us to describe an algebraic structure and provide canonical
  instances of that structure for various types.
* Context parameters are defined by starting a parameter list with the `using`
  keyword. At the call site, Scala will search for a given instance of each
  context parameter. Given instances are defined with the `given` keyword.
* Context parameters can be passed explicitly with the `using` keyword at the
  call site.
* Scala’s `given` instances and context parameters allow us to offload type-driven
  composition to the compiler.
* The `summon` method returns the given instance in scope for the supplied type
  parameter. If no such instance is available, compilation fails.
* The `Foldable` typeclass describes type constructors that support computing an
  output value by folding over their elements — that is, support `foldLeft`,
  `foldRight`, `foldMap`, and `combineAll`.

#### Chapter 11: Monads

> _**A monad is a monoid in a category of endofunctors**_

##### Minimal sets of monadic combinators

> A monad is an implementation of one of the minimal sets of monadic
> combinators, satisfying the laws of associativity and identity.

* `unit` and `flatMap`
* `unit` and `compose`
* `unit`, `map`, and `join`

##### State monad

```scala 3
def get[S]: State[S, S] = s => (s, s)
def set[S](s: S): State[S, Unit] = _ => ((), s)
```

> Remember that we also discovered that these combinators constitute a minimal
> set of primitive operations for `State`. So together with the monadic
> primitives (
> `unit` and `flatMap`), they completely specify everything we can do with
> the `State`
> data type. This is true in general for monads—they all have `unit`
> and `flatMap`,
> and each monad brings its own set of additional primitive operations that are
> specific to it.

##### Conclusion

* A **functor** is an implementation of `map` that preserves the structure of
  the data type.
* The **functor** laws are
  * Identity: `x.map(a => a) == x`
  * Composition: `x.map(f).map(g) == x.map(f andThen g)`
* A **monad** is an implementation of one of the minimal sets of _monadic
  combinators_, satisfying the laws of associativity and identity.
* The minimal sets of _monadic combinators_ are
  * `unit` and `flatMap`
  * `unit` and `compose`
  * `unit`, `map`, and `join`
* The _monad laws_ are
  * Associativity: `x.flatMap(f).flatMap(g) == x.flatMap(a => f(a).flatMap(g))`
  * Right identity: `x.flatMap(unit) == x`
  * Left identity: `unit(y).flatMap(f) == f(y)`
* All **monads** are **functors**, but not all functors are monads.
* There are monads for many of the data types encountered in this book,
  including `Option`, `List`, `LazyList`, `Par`, and `State[S, _]`.
* The `Monad` contract doesn’t specify what is happening between the lines, only
  that whatever is happening satisfies the laws of associativity and identity.
* Providing a `Monad` instance for a type constructor has practical usefulness.
  Doing so gives access to all of the derived operations (or combinators) in
  exchange for implementing one of the minimal sets of monadic combinators.

#### Chapter 12: Applicative and traversable functors

> Furthermore, we can now make `Monad[F]` a subtype of `Applicative[F]` by
> providing the default implementation of `map2` in terms of `flatMap`. This tells
> us that _**all monads are applicative functors**_.

##### Effects in FP

> Functional programmers often informally call type constructors like `Par`,
> `Option`, `List`, `Parser`, `Gen`, and so on effects. This usage is distinct
> from the
> term _side effect_, which _implies some violation of referential
transparency_.
> These types are called effects because they augment ordinary values with extra
> capabilities. (`Par` adds the ability to define parallel computation, `Option`
> adds
> the possibility of failure, and so on.) We sometimes use the terms _monadic
> effects_ or _applicative effects_ to mean types with an associated Monad or
> Applicative instance.
