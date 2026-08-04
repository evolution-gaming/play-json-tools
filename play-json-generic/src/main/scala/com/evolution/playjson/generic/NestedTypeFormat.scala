package com.evolution.playjson.generic

import play.api.libs.json.OFormat

/** The `type` value a subtype is written with, and the subtype it belongs to. */
final case class Discriminator(subtype: String, name: String)

object NestedTypeFormat {

  @deprecated(
    "Use NestedTypeFormat.of, which reports subtypes it cannot tell apart on the wire",
    "1.4.0")
  def apply[A](implicit reads: NestedTypeReads[A], writes: NestedTypeWrites[A]): OFormat[A] =
    OFormat(reads.reads(_), writes.writes(_))

  /**
    * An `OFormat` writing each subtype of `A` with a `type` field naming it.
    *
    * `Left` when a subtype has no name, or when one name covers several subtypes: either way the
    * subtypes cannot be told apart when read back. Which subtypes those are differs between the
    * Scala versions, see [[NestedTypeWrites]].
    */
  def of[A](implicit
    reads: NestedTypeReads[A],
    writes: NestedTypeWrites[A],
    discriminators: Discriminators[A]
  ): Either[String, OFormat[A]] = {
    val unnamed = discriminators.all.filter(_.name.isEmpty).map(_.subtype)

    val shared = discriminators.all.groupBy(_.name).collect {
      case (name, subtypes) if subtypes.size > 1 => s"$name for ${ subtypes.map(_.subtype).mkString(" and ") }"
    }

    if (unnamed.nonEmpty) {
      Left(
        s"NestedTypeFormat has no name for ${ unnamed.mkString(" or ") }. Nest the subtype in an " +
          "object, or use FlatTypeFormat, which names subtypes by their own class.")
    } else if (shared.nonEmpty) {
      Left(s"NestedTypeFormat gives one name to several subtypes: ${ shared.mkString("; ") }")
    } else {
      Right(OFormat(reads.reads(_), writes.writes(_)))
    }
  }
}
