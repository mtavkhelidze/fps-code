//noinspection ScalaWeakerAccess,ScalaUnusedSymbol
package ge.zgharbi.study.fps
package ch.c09

enum JsonError {
  case ExpectedComma(msg: String)
  case ExpectedColon(msg: String)
  case ExpectedValue(msg: String)
}
enum JSONGrammar {
  case JArray(get: IndexedSeq[JSONGrammar])
  case JBool(get: Boolean)
  case JNull
  case JNumber(get: Double)
  case JObject(get: Map[String, JSONGrammar])
  case JString(get: String)
}

object JSONGrammar {
  def jsonParser[Parser[+_]](P: Parsers[Parser]): Parser[JSONGrammar] = {
    import P.*

    def token(s: String): Parser[String] = string(s).token

    def value: Parser[JSONGrammar] = lit | obj | array

    def lit: Parser[JSONGrammar] = (
      token("null").as(JNull) |
        double.map(JNumber(_)) |
        escapedQuoted.map(JString(_)) |
        token("true").as(JBool(true)) |
        token("false").as(JBool(false))
    ).scope("literal")

    def keyVal: Parser[(String, JSONGrammar)] =
      escapedQuoted ** (token(":") *> value)

    def array: Parser[JSONGrammar] =
      (token("[") *>
        value.sep(token(",")).map(vs => JArray(vs.toIndexedSeq)) <*
        token("]")).scope("array")

    def obj: Parser[JSONGrammar] = (
      token("{") *>
        keyVal
          .sep(token(","))
          .map(kvs => JObject(kvs.toMap)) <* token("}")
    ).scope("object")

    (whitespace *> (obj | array)).root
  }
}
