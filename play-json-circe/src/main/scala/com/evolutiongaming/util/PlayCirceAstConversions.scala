package com.evolutiongaming.util

import cats.Eval
import io.circe.{Json => CirceJson}
import play.api.libs.{json => PlayJson}

object PlayCirceAstConversions {
  private type Field[T] = (String, T)

  private def evalZero[T]: Eval[Vector[T]] = Eval.now(Vector.empty[T])

  def circeToPlay(circeJson: CirceJson): PlayJson.JsValue = {
    def inner(json: Eval[CirceJson]): Eval[PlayJson.JsValue] =
      json.flatMap(_.fold(
        jsonNull = Eval.now(PlayJson.JsNull),
        jsonBoolean = b => Eval.now(PlayJson.JsBoolean(b)),
        jsonNumber = n => Eval.now(n.toBigDecimal.map(PlayJson.JsNumber).getOrElse(PlayJson.JsNumber(n.toDouble))),
        jsonString = s => Eval.now(PlayJson.JsString(s)),
        jsonArray = as =>
          Eval
            .defer {
              as.foldLeft(evalZero[PlayJson.JsValue])((acc, c) => inner(Eval.now(c)).flatMap(p => acc.map(_ :+ p)))
            }
            .map(PlayJson.JsArray),
        jsonObject = obj =>
          Eval
            .defer {
              obj.toIterable.foldLeft(evalZero[Field[PlayJson.JsValue]]) { case (acc, (k, c)) =>
                inner(Eval.now(c)).flatMap(p => acc.map(_ :+ (k -> p)))
              }
            }
            .map(PlayJson.JsObject)
      ))

    inner(Eval.now(circeJson)).value
  }

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
            Eval
              .defer {
                values.foldLeft(evalZero[CirceJson])((acc, p) => inner(Eval.now(p)).flatMap(c => acc.map(_ :+ c)))
              }
              .map(CirceJson.fromValues)

        case PlayJson.JsObject(value) =>
          Eval
            .defer {
              value.view.foldLeft(evalZero[Field[CirceJson]]) { case (acc, (k, p)) =>
                inner(Eval.now(p)).flatMap(c => acc.map(_ :+ (k -> c)))
              }
            }
            .map(CirceJson.fromFields)
      }

    inner(Eval.now(value)).value
  }

}
