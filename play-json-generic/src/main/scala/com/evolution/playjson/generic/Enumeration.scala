package com.evolution.playjson.generic

import play.api.libs.json._

@deprecated(
  "Use EnumerationFormat.of, which reports labels the naming strategy collapses onto one another. " +
    "This one gives them one label, leaving all but one value unreadable",
  "1.4.0")
class Enumeration[A] private(enumMappings: EnumMappings[A]) {

  def format(implicit nameCodingStrategy: NameCodingStrategy): Format[A] = new Format[A] {

    val labelsLookup: Map[A, String] = enumMappings.labels.map { case (k, v) => (k, nameCodingStrategy(v)) }
    val valuesLookup: Map[String, A] = labelsLookup.map(_.swap)

    def writes(o: A): JsValue = JsString(labelsLookup(o))

    def reads(json: JsValue): JsResult[A] = {
      for {
        s <- json.validate[JsString]
        v <- valuesLookup.get(s.value) match {
          case Some(v) => JsSuccess(v)
          case None    => JsError(s"Cannot parse ${ s.value }")
        }
      } yield v
    }
  }
}

@deprecated("Use EnumerationFormat.of, which validates the labels", "1.4.0")
object Enumeration {
  def apply[A](implicit enumMappings: EnumMappings[A]) = new Enumeration[A](enumMappings)
}
