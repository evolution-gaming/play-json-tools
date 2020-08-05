package com.evolutiongaming.jsonitertool

import java.io.{InputStream, OutputStream}

import com.github.plokhotnyuk.jsoniter_scala.core._
import play.api.libs.json._

import scala.util.Try

object PlayJsonJsoniter {

  implicit val jsValueCodec: JsonValueCodec[JsValue] = {
    val settings = JsonParserSettings.settings
    JsonValueCodecJsValue(settings.bigDecimalParseSettings)
  }

  def serialize(payload: JsValue): Array[Byte] =
    writeToArray(payload)

  def serializeToStr(payload: JsValue): String =
    writeToString(payload)

  def serializeToOutput(payload: JsValue, out: OutputStream): Unit =
    writeToStream(payload, out)

  def deserialize(bytes: Array[Byte]): Try[JsValue] =
    Try(readFromArray[JsValue](bytes))

  def deserializeFromStr(str: String): Try[JsValue] =
    Try(readFromString(str))

  def deserializeFromInput(in: InputStream): Try[JsValue] =
    Try(readFromStream(in))
}
