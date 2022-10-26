package com.evolutiongaming.util

import io.circe._
import io.circe.parser.parse
import play.api.libs.json._

object CirceToPlayConversions {

  private def convert(value: Json): JsValue = value.fold(
    jsonNull    = JsNull,
    jsonBoolean = x => JsBoolean(x),
    jsonNumber  = x => JsNumber(x.toDouble),
    jsonString  = x => JsString(x),
    jsonArray   = x => JsArray(x.map(convert)),
    jsonObject  = x => JsObject(x.toIterable.map { case (k, v) => (k, convert(v)) }.toSeq),
  )

  implicit def readsFromDecoder[T: Decoder]: Reads[T] = Reads { json =>
    (for {
      json <- parse(json.toString).left.map(err => JsError(err.message))
      res  <- implicitly[Decoder[T]].decodeJson(json).left.map(err => JsError(err.message))
    } yield JsSuccess(res)).merge
  }

  implicit def writesFromEncoder[T: Encoder]: Writes[T] = Writes { value =>
    convert(implicitly[Encoder[T]].apply(value))
  }
}

