package com.evolutiongaming.util.generic

trait NameCodingStrategy {
  def encode(s: String): String
  def decode(s: String): String
}

trait LowPriority {
  implicit val default: NameCodingStrategy = new NameCodingStrategy {
    override def encode(s: String): String = s
    override def decode(s: String): String = s
  }
}

object NameCodingStrategy extends LowPriority

object NameCodingStrategies {
  implicit val kebabCase: NameCodingStrategy = new NameCodingStrategy {
    override def encode(s: String): String =
      s.foldLeft(List.empty[String]) {
        case (ll, n) if n.isUpper =>
          n.toLower.toString :: ll
        case (h :: t, n)          =>
          h + n :: t
        case (nil, n)             =>
          n.toString :: nil
      }.reverse.mkString("-")

    override def decode(s: String): String = s.split("-").map(_.capitalize).mkString("")
  }
}
