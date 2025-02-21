package com.evolution.playjson.generic

import play.api.libs.json._

/**
  * This is a helper class for creating a `Format` instance for a sealed trait hierarchy.
  * 
  * When reading from JSON, it will look for a `type` field in the JSON object and use its value to determine which 
  * subtype to use. The `type` field will be removed from the JSON object before the subtype's `Reads` is called.
  * 
  * When writing to JSON, it will add a `type` field to the JSON object with the value of the subtype's simple name
  * (without package prefix).
  */
object FlatTypeFormat:
  def apply[A](using reads: FlatTypeReads[A], writes: FlatTypeWrites[A]): OFormat[A] =
    OFormat(reads.reads(_), writes.writes(_))
