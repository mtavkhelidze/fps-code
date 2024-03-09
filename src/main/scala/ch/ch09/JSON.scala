package ge.zgharbi.study.fps
package ch.ch09

enum JSON {
  case JArray(get: IndexedSeq[JSON])
  case JBool(get: Boolean)
  case JNull
  case JNumber(get: Double)
  case JObject(get: Map[String, JSON])
  case JString(get: String)
}

object JSON {
  def jsonParser[Parser[+_]](P: Parsers[Parser]): Parser[JSON] =
    import P.*
    def token(s: String): Parser[String] = string(s).token

    def value: Parser[JSON] = lit

    def lit: Parser[JSON] = (
      token("null").as(JNull) |
        double.map(JNumber(_)) |
        escapedQuoted.map(JString(_)) |
        token("true").as(JBool(true)) |
        token("false").as(JBool(false))
    ).scope("literal")

    def document: Parser[JSON] = whitespace *> (array | obj) <* eof
    def obj: Parser[JSON] = ???
    def array: Parser[JSON] =
      (token("[") *>
        value.sep(token(",")).map(vs => JArray(vs.toIndexedSeq)) <*
        token("]")).scope("array")
    ???
}
