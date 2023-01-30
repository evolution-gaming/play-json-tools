package com.evolutiongaming.util

import io.circe.{Json => CirceJson}
import play.api.libs.{json => PlayJson}

object PlayCirceAstConversions {
  def circeToPlay(circeJson: CirceJson): PlayJson.JsValue =
    circeJson.fold(
      jsonNull = PlayJson.JsNull,
      jsonBoolean = b => PlayJson.JsBoolean(b),
      jsonNumber = n => n.toBigDecimal.map(PlayJson.JsNumber).getOrElse(PlayJson.JsNumber(n.toDouble)),
      jsonString = s => PlayJson.JsString(s),
      jsonArray = as => PlayJson.JsArray(as.map(circeToPlay)),
      jsonObject = o => PlayJson.JsObject(o.toIterable.map { case (k, v) => (k, circeToPlay(v)) }.toSeq),
    )

  def playToCirce(value: PlayJson.JsValue): CirceJson =
    value match {
      case PlayJson.JsNull =>
        CirceJson.Null

      case PlayJson.JsTrue =>
        CirceJson.True

      case PlayJson.JsFalse =>
        CirceJson.False

      case PlayJson.JsNumber(value) =>
        CirceJson.fromBigDecimal(value)

      case PlayJson.JsString(value) =>
        CirceJson.fromString(value)

      case PlayJson.JsArray(value) =>
        CirceJson.fromValues(value.map(playToCirce))

      case PlayJson.JsObject(value) =>
        CirceJson.fromFields(value.iterator.map { case (k, v) => (k, playToCirce(v)) }.to(Iterable))
    }

}
