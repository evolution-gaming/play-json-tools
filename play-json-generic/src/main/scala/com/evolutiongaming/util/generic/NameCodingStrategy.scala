package com.evolutiongaming.util.generic

trait NameCodingStrategy {
  def encode(input: String): String
  def matches(encoded: String, compareAgainst: String): Boolean
}

trait LowPriority {
  implicit val default: NameCodingStrategy = new NameCodingStrategy {
    override def encode(input: String): String = input
    override def matches(encoded: String, compareAgainst: String): Boolean = encoded == compareAgainst
  }
}

object NameCodingStrategy extends LowPriority

object NameCodingStrategies {
  private def lowerCaseSepCoding(sep: String): NameCodingStrategy = new NameCodingStrategy {

    override def encode(input: String): String =
      input.foldLeft(List.empty[String]) {
        case (ll, n) if n.isUpper =>
          n.toLower.toString :: ll
        case (h :: t, n)          =>
          h + n :: t
        case (nil, n)             =>
          n.toString :: nil
      }.reverse.mkString(sep)

    override def matches(encoded: String, compareAgainst: String): Boolean =
      encoded == encode(compareAgainst)
  }

  implicit val kebabCase: NameCodingStrategy = lowerCaseSepCoding("-")

  implicit val snakeCase: NameCodingStrategy = lowerCaseSepCoding("_")

  implicit val noSepCase: NameCodingStrategy = lowerCaseSepCoding("")
}
