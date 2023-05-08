package com.evolution.playjson.generic

import play.api.libs.json.*

import scala.compiletime.*

/**
  * Summons an `OWrites[A]` instance for type `A` and enriches it with a `type` field.
  * The `type` fields contains the passed `name` value.
  * 
  * @param name the value for the `type` field
  * @tparam A the type to summon an `OWrites[A]` instance for
  */
private[generic] inline def summonEnrichedWrites[A](name: String): OWrites[A] =
  summonInline[OWrites[A]].transform(jsObject =>
    JsObject(Seq("type" -> JsString(name))) ++ jsObject
  )

/**
  * Prefixes the passed `name` with the passed `prefix` if the `prefix` is not blank.
  */
private[generic] inline def prefixName(prefix: String, name: String) =
  if prefix.isBlank() then name else s"$prefix.$name"

/**
  * Return the name of the given singleton type (object without `case` modifier). Originally,
  * `valueOf.value.toString()` returns something like `com.evolution.playjson.generic.Message$Out$Ack$@307d9c1d`
  * which is why we have to split it, drop the last element and take "len - 1" element
  */  
private[generic] inline def singletonName[A](using valueOf: ValueOf[A]): String =
  valueOf.value.toString().split("\\$").dropRight(1).last
