package com.evolutiongaming.util.generic

trait NameCodingStrategy extends ((String) => String)

trait LowPriority {
  implicit val default: NameCodingStrategy = identity[String]
}

object NameCodingStrategy extends LowPriority

object NameCodingStrategies {
  private def lowerCaseSepCoding(sep: String): NameCodingStrategy =
    _.split("(?<!^)(?=[A-Z])").map(_.toLowerCase).mkString(sep)

  implicit val kebabCase: NameCodingStrategy = lowerCaseSepCoding("-")

  implicit val snakeCase: NameCodingStrategy = lowerCaseSepCoding("_")

  implicit val noSepCase: NameCodingStrategy = _.toLowerCase

  implicit val upperCase: NameCodingStrategy = _.toUpperCase
}
