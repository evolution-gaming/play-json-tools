package com.evolution.playjson.generic

trait NameCodingStrategy extends ((String) => String)

trait LowPriority:
  given default: NameCodingStrategy = new NameCodingStrategy() {
    override def apply(s: String): String = s
  }

object NameCodingStrategy extends LowPriority

object NameCodingStrategies:

  private def lowerCaseSepCoding(sep: String): NameCodingStrategy = new NameCodingStrategy() {
    override def apply(s: String): String = s.split("(?<!^)(?=[A-Z])").map(_.toLowerCase).mkString(sep)
  }

  given kebabCase: NameCodingStrategy = lowerCaseSepCoding("-")

  given snakeCase: NameCodingStrategy = lowerCaseSepCoding("_")

  given noSepCase: NameCodingStrategy = new NameCodingStrategy() {
    override def apply(s: String): String = s.toLowerCase
  }

  given upperCase: NameCodingStrategy = new NameCodingStrategy() {
    override def apply(s: String): String = s.toUpperCase
  }