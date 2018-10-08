package com.evolutiongaming.util.generic

import play.api.libs.json._

class Enumeration[A] private(enumMappings: EnumMappings[A]) {

  def format(implicit nameCodingStrategy: NameCodingStrategy): Format[A] = new Format[A] {

    val labelsLookup = enumMappings.labels.mapValues(nameCodingStrategy)
    val valuesLookup = labelsLookup.map(_.swap)

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

object Enumeration {
  def apply[A](implicit enumMappings: EnumMappings[A]) = new Enumeration[A](enumMappings)
}
