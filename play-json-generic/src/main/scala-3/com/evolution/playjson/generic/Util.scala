package com.evolution.playjson.generic

import play.api.libs.json.*

import scala.compiletime.*

/** Summons an `OWrites[A]` instance for type `A` and enriches it with a `type` field.
  * The `type` fields contains the passed `name` value.
  *
  * @param name the value for the `type` field
  * @tparam A the type to summon an `OWrites[A]` instance for
  */
private[generic] inline def summonEnrichedWrites[A](name: String): OWrites[A] =
  summonInline[OWrites[A]].transform(jsObject =>
    JsObject(Seq("type" -> JsString(name))) ++ jsObject
  )

/** Prefixes the passed `name` with the passed `prefix` if the `prefix` is not blank.
  */
private[generic] inline def prefixName(prefix: String, name: String) =
  if prefix.isBlank() then name else s"$prefix.$name"

/** Return the name of the given singleton type (object without `case` modifier). The class is named
  * like `com.evolution.playjson.generic.Message$Out$Ack$`, so the last `$` separated segment is the
  * object itself. Taken from the class rather than from `toString`, which an object is free to
  * override and which would then name the subtype after arbitrary text, or fail to compile where the
  * override is declared without parentheses.
  *
  * An object declared at package level has no `$` before its name, so the segment still carries the
  * package: `com.example.Ping` rather than `Ping`.
  */
private[generic] inline def singletonName[A](using valueOf: ValueOf[A]): String =
  valueOf.value.getClass.getName.split("\\$").last
