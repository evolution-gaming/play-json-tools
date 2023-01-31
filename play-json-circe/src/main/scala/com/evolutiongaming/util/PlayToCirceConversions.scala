package com.evolutiongaming.util

import com.evolutiongaming.util.PlayCirceAstConversions._
import io.circe._
import play.api.libs.json._

object PlayToCirceConversions {

  implicit def decoderFromReads[T](implicit reads: Reads[T]): Decoder[T] =
    Decoder.decodeJson.emap { circeAst =>
      circeToPlay(circeAst).validate[T] match {
        case JsSuccess(value, _) =>
          Right(value)
        case JsError(errors) =>
          Left(errors.flatMap { case (_, errs) => errs }.map(_.message).mkString(", "))
      }
    }

  implicit def encoderFromWrites[T](implicit writes: Writes[T]): Encoder[T] =
    Encoder.instance { value =>
      playToCirce(writes.writes(value))
    }
}

