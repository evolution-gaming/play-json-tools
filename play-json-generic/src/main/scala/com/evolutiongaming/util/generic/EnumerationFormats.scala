package com.evolutiongaming.util.generic

import play.api.libs.json._

object EnumerationFormats {

  def apply[A](implicit enumMappings: EnumMappings[A]): Format[A] = new Format[A] {

    val valuesLookup = enumMappings.labels.map(_.swap)

    def writes(o: A): JsValue = JsString(enumMappings.labels(o))

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
