package com.evolution.playjson.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core._
import play.api.libs.json._

import java.io.{InputStream, OutputStream}
import java.nio.ByteBuffer
import java.time._
import scala.util.Try

object PlayJsonJsoniter {
  implicit val jsValueCodec: JsonValueCodec[JsValue] = {
    val settings = JsonConfig.settings
    JsonValueCodecJsValue(settings.bigDecimalParseConfig, settings.bigDecimalSerializerConfig)
  }
  implicit val durationFormat: Format[Duration] =
    Formats.smallAsciiStringFormat[Duration]("Period", _.readDuration(_), _.writeVal(_))
  implicit val instantFormat: Format[Instant] =
    Formats.smallAsciiStringFormat[Instant]("Period", _.readInstant(_), _.writeVal(_))
  implicit val localDateFormat: Format[LocalDate] =
    Formats.smallAsciiStringFormat[LocalDate]("LocalDate", _.readLocalDate(_), _.writeVal(_))
  implicit val localDateTimeFormat: Format[LocalDateTime] =
    Formats.smallAsciiStringFormat[LocalDateTime]("LocalDateTime", _.readLocalDateTime(_), _.writeVal(_))
  implicit val localTimeFormat: Format[LocalTime] =
    Formats.smallAsciiStringFormat[LocalTime]("LocalTime", _.readLocalTime(_), _.writeVal(_))
  implicit val monthDayFormat: Format[MonthDay] =
    Formats.smallAsciiStringFormat[MonthDay]("MonthDay", _.readMonthDay(_), _.writeVal(_))
  implicit val offsetDateTimeFormat: Format[OffsetDateTime] =
    Formats.smallAsciiStringFormat[OffsetDateTime]("OffsetDateTime", _.readOffsetDateTime(_), _.writeVal(_))
  implicit val offsetTimeFormat: Format[OffsetTime] =
    Formats.smallAsciiStringFormat[OffsetTime]("OffsetTime", _.readOffsetTime(_), _.writeVal(_))
  implicit val periodFormat: Format[Period] =
    Formats.smallAsciiStringFormat[Period]("Period", _.readPeriod(_), _.writeVal(_))
  implicit val yearFormat: Reads[Year] =
    Formats.smallAsciiStringFormat[Year]("Year", _.readYear(_), _.writeVal(_))
  implicit val yearMonthFormat: Format[YearMonth] =
    Formats.smallAsciiStringFormat[YearMonth]("YearMonth", _.readYearMonth(_), _.writeVal(_))
  implicit val zonedDateTimeFormat: Format[ZonedDateTime] =
    Formats.smallAsciiStringFormat[ZonedDateTime]("ZonedDateTime", _.readZonedDateTime(_), _.writeVal(_))

  /**
    * Throws `JsonWriterException` for a number that could not be read back, see
    * [[play.api.libs.json.JsonValueCodecJsValue]].
    */
  def serialize(payload: JsValue): Array[Byte] =
    writeToArray(payload)

  /** Throws `JsonWriterException` for a number that could not be read back, see [[serialize]]. */
  def serializeToStr(payload: JsValue): String =
    writeToString(payload)

  /**
    * Throws `JsonWriterException` for a number that could not be read back, see [[serialize]].
    * The failure is raised where the number is reached, so `bbuf` is left holding the part of the
    * document written up to that point.
    */
  def serializeToBuffer(payload: JsValue, bbuf: ByteBuffer): Unit =
     writeToByteBuffer(payload, bbuf)

  /**
    * Throws `JsonWriterException` for a number that could not be read back, see [[serialize]].
    * The failure is raised where the number is reached, and the writer flushes as its buffer
    * fills, so `out` may already have received an incomplete document.
    */
  def serializeToOutput(payload: JsValue, out: OutputStream): Unit =
    writeToStream(payload, out)

  def deserialize(bytes: Array[Byte]): Try[JsValue] =
    Try(readFromArray[JsValue](bytes))

  def deserializeFromStr(str: String): Try[JsValue] =
    Try(readFromString(str))

  def deserializeFromInput(in: InputStream): Try[JsValue] =
    Try(readFromStream(in))

  def deserializeFromBuffer(bbuf: ByteBuffer): Try[JsValue] =
    Try(readFromByteBuffer(bbuf))
}
