package com.evolution.playjson.generic

import play.api.libs.json.OFormat

/** The `type` value a subtype is written with, and the subtype it belongs to. */
final case class Discriminator(subtype: String, name: String)

object NestedTypeFormat {

  @deprecated(
    "Use NestedTypeFormat.of, which reports subtypes it cannot tell apart on the wire",
    "1.4.0"
  )
  def apply[A](implicit reads: NestedTypeReads[A], writes: NestedTypeWrites[A]): OFormat[A] =
    OFormat(reads.reads(_), writes.writes(_))

  /** An `OFormat` writing each subtype of `A` with a `type` field naming it.
    *
    * `Left` only when one name covers several subtypes, which is the case that cannot be read back:
    * whichever subtype is tried first wins and the others are unreachable. A single subtype with an
    * empty name is left alone, unhelpful as that name is, because it round-trips and earlier
    * versions wrote it. Which subtypes end up sharing a name differs between the Scala versions,
    * see [[NestedTypeWrites]].
    */
  def of[A](implicit
      reads: NestedTypeReads[A],
      writes: NestedTypeWrites[A],
      discriminators: Discriminators[A]
  ): Either[String, OFormat[A]] = {
    val shared = discriminators.all.groupBy(_.name).collect {
      case (name, subtypes) if subtypes.size > 1 =>
        s"${describe(name)} for ${subtypes.map(_.subtype).mkString(" and ")}"
    }

    if (shared.isEmpty) Right(OFormat(reads.reads(_), writes.writes(_)))
    else {
      Left(
        "NestedTypeFormat gives one name to several subtypes, so reading a document can only ever " +
          s"find the first of them: ${shared.mkString("; ")}"
      )
    }
  }

  private def describe(name: String): String = if (name.isEmpty) "an empty name" else name
}
