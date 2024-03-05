def fn() = {
  println("fn is called")
  "value"
}

def lazyFn(v: => String): Unit = {
  println(s"from LazyFn: $v")
}

lazyFn(fn())
