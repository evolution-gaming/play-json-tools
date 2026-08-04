package com.evolution.playjson.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, JsonWriterException, writeToString}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

import java.math.MathContext
import java.nio.charset.StandardCharsets
import scala.util.Success

/**
 * Contract tests for the BigDecimal handling of [[PlayJsonJsoniter]].
 *
 * The contract is behavior parity with play-json plus round-trip consistency:
 *
 *   - serialization produces the same bytes as play-json's own JVM serializer: trailing zeros are
 *     stripped and values outside `[minPlain, maxPlain]` of
 *     `JsonConfig.settings.bigDecimalSerializerConfig` are written in scientific notation.
 *   - every value the codec serializes it can also deserialize under the limits of
 *     `JsonConfig.settings.bigDecimalParseConfig` (digitsLimit, scaleLimit), when no
 *     representation of a value fits those limits, serialization fails fast instead of
 *     producing bytes that cannot be read back.
 *   - reading applies `MathContext.DECIMAL128` exactly like play-json, so values with more
 *     than 34 significant digits deserialize to their rounded form.
 */
class JsoniterBigDecimalRoundTripSpec extends AnyFunSuite with Matchers {

  // one significant digit, but 313 plain characters once scale is 2, must be written as "1E+309"
  private val oneDigitLargeMagnitude = BigDecimal("1e309").setScale(2)

  // all 312 digits significant: no representation fits digitsLimit (310)
  private val beyondDigitsLimit = BigDecimal("9" * 310 + ".25")

  // 41 significant digits: within write limits, rounded to DECIMAL128 (34 digits) on read
  private val beyondDecimal128 = BigDecimal("1." + "1" * 40)

  // 7 characters in scientific notation, but scale 7000 exceeds scaleLimit (6178) on read
  private val beyondScaleLimit = BigDecimal("1E-7000")

  private def jsonOf(value: BigDecimal): JsObject =
    JsObject(Map("amount" -> JsNumber(value)))

  private def roundTrip(value: BigDecimal): Unit = {
    val jsValue = jsonOf(value)
    val bytes = PlayJsonJsoniter.serialize(jsValue)
    PlayJsonJsoniter.deserialize(bytes) shouldEqual Success(jsValue)
    ()
  }

  private def assertParityWithPlayJson(value: BigDecimal): Unit = {
    val jsValue = jsonOf(value)
    new String(PlayJsonJsoniter.serialize(jsValue), StandardCharsets.UTF_8) shouldEqual
      new String(Json.toBytes(jsValue), StandardCharsets.UTF_8)
    ()
  }

  test("round-trips a plain value") {
    roundTrip(BigDecimal("123.45"))
  }

  test("round-trips a one-significant-digit value of large magnitude") {
    roundTrip(oneDigitLargeMagnitude)
  }

  test("serializes plain values identically to play-json") {
    assertParityWithPlayJson(BigDecimal("123.45"))
  }

  test("serializes values with trailing zeros identically to play-json") {
    assertParityWithPlayJson(BigDecimal("100.00"))
  }

  test("serializes large-magnitude values identically to play-json") {
    assertParityWithPlayJson(oneDigitLargeMagnitude)
  }

  test("rejects serialization when no representation fits the read digits limit") {
    a[JsonWriterException] should be thrownBy {
      PlayJsonJsoniter.serialize(jsonOf(beyondDigitsLimit))
    }
  }

  test("rejects serialization when the scale exceeds the read scale limit") {
    a[JsonWriterException] should be thrownBy {
      PlayJsonJsoniter.serialize(jsonOf(beyondScaleLimit))
    }
  }

  // The reader rejects at `digits >= digitsLimit` and `abs(scale) >= scaleLimit`, and the writer
  // mirrors it. These four tests stand on either side of those two boundaries, so a jsoniter
  // upgrade that moves one by a single digit fails the build instead of going unnoticed.

  test("writes a value with one digit fewer than the read digits limit") {
    val digitsLimit = JsonConfig.settings.bigDecimalParseConfig.digitsLimit
    val largest = BigDecimal("1" + "0" * (digitsLimit - 3) + "7")

    largest.precision shouldEqual digitsLimit - 1
    PlayJsonJsoniter.deserialize(PlayJsonJsoniter.serialize(jsonOf(largest))) shouldBe a[Success[_]]
  }

  test("rejects a value with exactly as many digits as the read digits limit") {
    val digitsLimit = JsonConfig.settings.bigDecimalParseConfig.digitsLimit
    val tooLong = BigDecimal("1" + "0" * (digitsLimit - 2) + "7")

    tooLong.precision shouldEqual digitsLimit
    a[JsonWriterException] should be thrownBy PlayJsonJsoniter.serialize(jsonOf(tooLong))
  }

  test("writes a value with one scale fewer than the read scale limit") {
    val scaleLimit = JsonConfig.settings.bigDecimalParseConfig.scaleLimit

    PlayJsonJsoniter.deserialize(PlayJsonJsoniter.serialize(jsonOf(BigDecimal(1, scaleLimit - 1)))) shouldBe
      a[Success[_]]
    PlayJsonJsoniter.deserialize(PlayJsonJsoniter.serialize(jsonOf(BigDecimal(1, -(scaleLimit - 1))))) shouldBe
      a[Success[_]]
  }

  test("rejects a value whose scale reaches the read scale limit") {
    val scaleLimit = JsonConfig.settings.bigDecimalParseConfig.scaleLimit

    a[JsonWriterException] should be thrownBy PlayJsonJsoniter.serialize(jsonOf(BigDecimal(1, scaleLimit)))
    a[JsonWriterException] should be thrownBy PlayJsonJsoniter.serialize(jsonOf(BigDecimal(1, -scaleLimit)))
  }

  test("writes what play-json writes, even where play-json cannot read it back") {
    // play-json's serializer and its parser disagree about how to count the digits of a number
    // this long, so play-json rejects its own output here; byte parity means inheriting that
    val borderline = BigDecimal("-0.0" + "3" * 307)
    val bytes = PlayJsonJsoniter.serialize(jsonOf(borderline))

    new String(bytes, StandardCharsets.UTF_8) shouldEqual
      new String(Json.toBytes(jsonOf(borderline)), StandardCharsets.UTF_8)
    PlayJsonJsoniter.deserialize(bytes) shouldEqual Success(jsonOf(borderline.round(MathContext.DECIMAL128)))
    a[RuntimeException] should be thrownBy Json.parse(bytes)
  }

  test("drops the scale of a value whose trailing zeros are stripped") {
    // the round trip is by value, not by scale: play-json writes "100" for either of these
    new String(PlayJsonJsoniter.serialize(jsonOf(BigDecimal("100.00"))), StandardCharsets.UTF_8) shouldEqual
      """{"amount":100}"""
    PlayJsonJsoniter.deserialize(PlayJsonJsoniter.serialize(jsonOf(BigDecimal("100.00")))) shouldEqual
      Success(jsonOf(BigDecimal("100")))
  }

  test("keeps one decimal place when the serializer settings preserve it") {
    val settings = BigDecimalSerializerConfig(
      minPlain = JsonConfig.settings.bigDecimalSerializerConfig.minPlain,
      maxPlain = JsonConfig.settings.bigDecimalSerializerConfig.maxPlain,
      preserveZeroDecimal = true)
    implicit val codec: JsonValueCodec[JsValue] =
      JsonValueCodecJsValue(JsonConfig.settings.bigDecimalParseConfig, settings)

    writeToString[JsValue](jsonOf(BigDecimal("100.00"))) shouldEqual """{"amount":100.0}"""
  }

  test("reads high-precision values with DECIMAL128 rounding, same as play-json") {
    val bytes = PlayJsonJsoniter.serialize(jsonOf(beyondDecimal128))
    val rounded = beyondDecimal128.round(MathContext.DECIMAL128)

    PlayJsonJsoniter.deserialize(bytes) shouldEqual Success(jsonOf(rounded))
    PlayJsonJsoniter.deserialize(bytes) shouldEqual Success(Json.parse(bytes))
  }
}
