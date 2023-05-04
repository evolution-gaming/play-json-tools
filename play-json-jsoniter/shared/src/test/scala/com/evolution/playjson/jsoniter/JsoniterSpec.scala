package com.evolution.playjson.jsoniter

import TestData.DataLine
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsSuccess, Json, JsonParserSettings}

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import scala.util.Success
import scala.util.control.NonFatal

class JsoniterSpec extends AnyFunSuite with Matchers {
  val isJS: Boolean = 1.0.toString == "1"
  val maxDoubleStr = "179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368"

  test("Write using PlayJson -> Read using Jsoniter: Compare bytes") {
    val expected: DataLine = Json.fromJson[DataLine](Json.parse(TestData.jsonBody))
      .fold(errs => throw new Exception(s"Parsing error: ${errs.mkString(",")}"), identity)
    val jsValue = Json.toJson(expected)
    val bts0 = PlayJsonJsoniter.serialize(jsValue)
    val bts1 = Json.toBytes(jsValue)
    new String(bts0, "UTF-8") shouldEqual new String(bts1, "UTF-8")
  }

  test("Write using PlayJson -> Read using Jsoniter: Compare objects") {
    val expected: DataLine = Json.fromJson[DataLine](Json.parse(TestData.jsonBody))
      .fold(errs => throw new Exception(s"Parsing error: ${errs.mkString(",")}"), identity)
    val bts = Json.toBytes(Json.toJson(expected))
    val jsValue = PlayJsonJsoniter.deserialize(bts).map(Json.fromJson[DataLine](_))
    Success(JsSuccess(expected)) shouldEqual jsValue
  }

  test("Can write/read large number by play-json") {
    //when number size hits length 35, equality comparison doesn't work anymore
    val largeNum = "9999999999999999999999999999999911"
    val jsValue = play.api.libs.json.JsNumber(BigDecimal(largeNum))
    val bytes = Json.toBytes(jsValue)
    new String(bytes) shouldEqual largeNum
    if (!isJS) Json.parse(bytes) shouldEqual jsValue
  }

  test("Can write/read large number by jsoniter without loosing precision") {
    val largeNum = "9999999999999999999999999999999911"
    val jsValue = play.api.libs.json.JsNumber(BigDecimal(largeNum))
    val bytes = PlayJsonJsoniter.serialize(jsValue)
    new String(bytes) shouldEqual largeNum
    PlayJsonJsoniter.deserialize(bytes) shouldEqual Success(jsValue)
  }

  test("Can parse max double string as play json") {
    val bytes = s"""{ "max":$maxDoubleStr }""".getBytes
    if (!isJS) PlayJsonJsoniter.deserialize(bytes) shouldEqual Success(Json.parse(bytes))
  }

  test("PlayJson and Jsoniter can parse max double") {
    val bytes = maxDoubleStr.getBytes
    if (!isJS) PlayJsonJsoniter.deserialize(bytes) shouldEqual Success(Json.parse(bytes))
  }

  test("PlayJson and Jsoniter can parse negative double max") {
    val bytes = ("-" + maxDoubleStr).getBytes
    JsonParserSettings.settings.bigDecimalParseSettings.digitsLimit shouldEqual maxDoubleStr.length + 1
    if (!isJS) PlayJsonJsoniter.deserialize(bytes) shouldEqual Success(Json.parse(bytes))
  }

  test("Jsoniter can deserialize from string") {
    PlayJsonJsoniter.deserializeFromStr(TestData.jsonBody) shouldEqual Success(Json.parse(TestData.jsonBody))
  }

  test("Jsoniter can deserialize from InputStream") {
    var in: InputStream = null
    try {
      in = new ByteArrayInputStream(TestData.jsonBody.getBytes(StandardCharsets.UTF_8))
      PlayJsonJsoniter.deserializeFromInput(in) shouldEqual Success(Json.parse(TestData.jsonBody))
    } catch {
      case NonFatal(ex) => fail(ex)
    } finally {
      if (in ne null)
        in.close()
    }
  }

  test("PlayJsonJsoniter: serializeToBuffer -> deserializeFromBuffer") {
    val jsValue = Json.parse(TestData.jsonBody)
    val buf = ByteBuffer.allocate(1 << 10)
    PlayJsonJsoniter.serializeToBuffer(jsValue, buf)
    buf.position() should be > 0
    buf.flip()
    PlayJsonJsoniter.deserializeFromBuffer(buf) shouldEqual Success(jsValue)
  }
}