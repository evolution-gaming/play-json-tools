package com.evolution.playjson.generic

import play.api.libs.json._

/** Reads and writes a sealed hierarchy with a `type` field naming the subtype by its own name, with
  * no trace of where it is nested, and with the ambient [[NameCodingStrategy]] applied: `Ping` is
  * written as `Ping` by default and as `ping` with `NameCodingStrategies.kebabCase` in scope. Both
  * directions apply the same strategy, so a format reading documents has to be built under the
  * strategy they were written with.
  *
  * Because the name is the subtype's alone, two subtypes of the same simple name in different objects
  * are given one name, and nothing here reports it: the second is written happily and then fails to
  * read, since it is read against the fields of the first. [[NestedTypeFormat.of]] refuses that case;
  * this one has no equivalent yet.
  *
  * [[NestedTypeFormat]] is the alternative: it names subtypes by their nesting and pays no attention
  * to the naming strategy. The two write different `type` values for the same subtype, so they are a
  * choice made once per hierarchy rather than something to switch later.
  */
object FlatTypeFormat {
  def apply[A](implicit reads: FlatTypeReads[A], writes: FlatTypeWrites[A]): OFormat[A] =
    OFormat(reads.reads(_), writes.writes(_))
}
