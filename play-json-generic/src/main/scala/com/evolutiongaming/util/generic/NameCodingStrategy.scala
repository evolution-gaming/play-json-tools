package com.evolutiongaming.util.generic

trait NameCodingStrategy extends ((String) => String)

trait LowPriority {
  implicit val default: NameCodingStrategy = new NameCodingStrategy() {
    override def apply(s: String): String = s
  }
}

object NameCodingStrategy extends LowPriority

object NameCodingStrategies {

  private def lowerCaseSepCoding(sep: String): NameCodingStrategy = new NameCodingStrategy() {
    override def apply(s: String): String = s.split("(?<!^)(?=[A-Z])").map(_.toLowerCase).mkString(sep)
  }

  implicit val kebabCase: NameCodingStrategy = lowerCaseSepCoding("-")

  implicit val snakeCase: NameCodingStrategy = lowerCaseSepCoding("_")

  implicit val noSepCase: NameCodingStrategy = new NameCodingStrategy() {
    override def apply(s: String): String = s.toLowerCase
  }

  implicit val upperCase: NameCodingStrategy = new NameCodingStrategy() {
    override def apply(s: String): String = s.toUpperCase
  }
}