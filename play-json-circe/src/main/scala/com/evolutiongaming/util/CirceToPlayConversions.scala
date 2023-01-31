package com.evolutiongaming.util

import com.evolutiongaming.util.PlayCirceAstConversions._
import io.circe._
import play.api.libs.json._

object CirceToPlayConversions {

  implicit def readsFromDecoder[T](implicit decoder: Decoder[T]): Reads[T] =
    Reads { playAst =>
      decoder.decodeJson(playToCirce(playAst)) match {
        case Right(success) => JsSuccess(success)
        case Left(err) => JsError(err.message)
      }
    }

  implicit def writesFromEncoder[T](implicit encoder: Encoder[T]): Writes[T] =
    Writes { value =>
      circeToPlay(encoder(value))
    }
}

