package com.evolutiongaming.util

import io.circe._
import io.circe.syntax._
import play.api.libs.json.{Json => PlayJson, _}

import scala.util.Try

object PlayCirceConversions {

  private def convert(value: JsValue): Json = {
    import io.circe.Encoder._

    value match {
      case JsNull          =>
        None.asJson
      case JsTrue          =>
        true.asJson
      case JsFalse         =>
        false.asJson
      case JsNumber(value) =>
        value.asJson
      case JsString(value) =>
        value.asJson
      case JsArray(value)  =>
        value.map(convert).asJson
      case JsObject(value) =>
        JsonObject.fromIterable(value.iterator.map { case (k, v) => (k, convert(v)) }.to(Iterable)).asJson
    }
  }

  implicit def decoderFromReads[T: Reads]: Decoder[T] = Decoder.decodeJson.emap { json =>
    Try(PlayJson.parse(json.noSpaces).validate[T]).toEither.left.map(_.getMessage).flatMap {
      case JsSuccess(value, _) =>
        Right(value)
      case JsError(errors) =>
        Left(
          errors
            .flatMap { case (_, errs) => errs }
            .map(_.message)
            .mkString(", "),
        )
    }
  }

  implicit def encoderFromWrites[T: Writes]: Encoder[T] = Encoder.instance { value =>
    convert(implicitly[Writes[T]].writes(value))
  }
}

