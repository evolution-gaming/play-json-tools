package com.evolution.playjson.generic

import play.api.libs.json._

class Enumeration[A] private (enumMappings: EnumMappings[A]) {

  def format(implicit nameCodingStrategy: NameCodingStrategy): Format[A] = new Format[A] {

    val labelsLookup: Map[A, String] = enumMappings.labels.map { case (k, v) => (k, nameCodingStrategy(v)) }
    val valuesLookup: Map[String, A] = labelsLookup.map(_.swap)

    def writes(o: A): JsValue = JsString(labelsLookup(o))

    def reads(json: JsValue): JsResult[A] = {
      for {
        s <- json.validate[JsString]
        v <- valuesLookup.get(s.value) match {
          case Some(v) => JsSuccess(v)
          case None    => JsError(s"Cannot parse ${s.value}")
        }
      } yield v
    }
  }
}

object Enumeration {

  @deprecated(
    "Use EnumerationFormat.of, which validates the labels, or Enumeration.unsafe to keep this " +
      "behaviour without the warning",
    "1.5.0"
  )
  def apply[A](implicit enumMappings: EnumMappings[A]): Enumeration[A] = unsafe[A]

  /** Same as the deprecated `apply`: a label that several values collapse onto leaves all but one
    * of them unreadable.
    */
  def unsafe[A](implicit enumMappings: EnumMappings[A]): Enumeration[A] = new Enumeration[A](enumMappings)
}
