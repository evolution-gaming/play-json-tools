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
    JsonValueCodecJsValue(settings.bigDecimalParseConfig)
  }
  implicit val durationFormat: Format[Duration] =
    Formats.smallAsciiStringFormat[Duration]("Duration", _.readDuration(_), _.writeVal(_))
  implicit val instantFormat: Format[Instant] =
    Formats.smallAsciiStringFormat[Instant]("Instant", _.readInstant(_), _.writeVal(_))
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
  implicit val yearJsonFormat: Format[Year] =
    Formats.smallAsciiStringFormat[Year]("Year", _.readYear(_), _.writeVal(_))
  // `Year` was the only type here exposed as a `Reads`, so the implicit had to move to a new name:
  // widening this one to `Format` would change its erased result type and break linkage for code
  // compiled against 1.3.x. It is no longer implicit, which costs no binary compatibility, and can
  // be dropped at the next major bump.
  @deprecated("Use yearJsonFormat, which can write as well as read", "1.4.0")
  val yearFormat: Reads[Year] = yearJsonFormat
  implicit val yearMonthFormat: Format[YearMonth] =
    Formats.smallAsciiStringFormat[YearMonth]("YearMonth", _.readYearMonth(_), _.writeVal(_))
  implicit val zonedDateTimeFormat: Format[ZonedDateTime] =
    Formats.smallAsciiStringFormat[ZonedDateTime]("ZonedDateTime", _.readZonedDateTime(_), _.writeVal(_))

  def serialize(payload: JsValue): Array[Byte] =
    writeToArray(payload)

  def serializeToStr(payload: JsValue): String =
    writeToString(payload)

  def serializeToBuffer(payload: JsValue, bbuf: ByteBuffer): Unit =
     writeToByteBuffer(payload, bbuf)

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
