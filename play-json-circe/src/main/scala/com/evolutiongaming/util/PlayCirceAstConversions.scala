package com.evolutiongaming.util

import cats.Eval
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

  def playToCirce(value: PlayJson.JsValue): CirceJson = {
    def inner(value: Eval[PlayJson.JsValue]): Eval[CirceJson] =
      value.flatMap {
        case PlayJson.JsNull =>
          Eval.now(CirceJson.Null)

        case PlayJson.JsTrue =>
          Eval.now(CirceJson.True)

        case PlayJson.JsFalse =>
          Eval.now(CirceJson.False)

        case PlayJson.JsNumber(value) =>
          Eval.now(CirceJson.fromBigDecimal(value))

        case PlayJson.JsString(value) =>
          Eval.now(CirceJson.fromString(value))

        case PlayJson.JsArray(values) =>
          if (values.isEmpty)
            Eval.now(CirceJson.arr())
          else
            values.map(v => inner(Eval.now(v)))
              .foldLeft(Eval.now(Vector.empty[CirceJson]))((acc, v) => v.flatMap(v => acc.map(_ :+ v)))
              .map(CirceJson.fromValues)

        case PlayJson.JsObject(value) =>
          Eval.defer {
            value.view.map { case (k, v) =>
              inner(Eval.now(v)).map(k -> _)
            }.foldLeft(Eval.now(Map.empty[String, CirceJson]))((acc, v) => v.flatMap(v => acc.map(_ + v)))
              .map(CirceJson.fromFields)
          }
      }
    inner(Eval.now(value)).value
  }

}
