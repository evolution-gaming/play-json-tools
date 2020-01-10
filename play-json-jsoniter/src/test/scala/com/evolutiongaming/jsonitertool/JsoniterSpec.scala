package com.evolutiongaming.jsonitertool

import com.evolutiongaming.jsonitertool.TestData.DataLine
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsSuccess, Json, JsonParserSettings}
import TestData._

import scala.util.Success

class JsoniterSpec extends AnyFunSuite with Matchers {

  val maxDoubleStr = "179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368"

  test("Write using PlayJson -> Read using Jsoniter: Compare bytes") {

    val expected: DataLine = Json.fromJson[DataLine](Json.parse(TestData.jsonBody))
      .fold(errs => throw new Exception(s"Parsing error: ${errs.mkString(",")}"), identity)

    val jsValue = Json.toJson(expected)
    val bts0 = PlayJsonJsoniter.serialize(jsValue)
    val bts1 = Json.toBytes(jsValue)
    java.util.Arrays.compare(bts0, bts1) shouldEqual 0
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
    Json.parse(bytes) shouldEqual jsValue
  }

  test("Can write/read large number by jsoniter without loosing precision") {
    val largeNum = "9999999999999999999999999999999911"
    val jsValue = play.api.libs.json.JsNumber(BigDecimal(largeNum))
    val bytes = PlayJsonJsoniter.serialize(jsValue)
    new String(bytes) shouldEqual largeNum
    PlayJsonJsoniter.deserialize(bytes) shouldEqual Success(jsValue)
  }

  test("Can parse max double string as play json") {
    val json = s"""{ "max":$maxDoubleStr }"""
    val jsValue0 = Json.parse(json.getBytes)
    jsValue0 shouldEqual PlayJsonJsoniter.deserialize(json.getBytes).get
  }

  test("PlayJson and Jsoniter can parse max double") {
    val maxDouble = maxDoubleStr
    val jsValue0 = Json.parse(maxDouble.getBytes)
    jsValue0 shouldEqual PlayJsonJsoniter.deserialize(maxDouble.getBytes).get
  }

  test("PlayJson and Jsoniter can parse negative double max") {
    val maxDouble = "-" + maxDoubleStr
    val jsValue0 = Json.parse(maxDouble.getBytes)
    JsonParserSettings.settings.bigDecimalParseSettings.digitsLimit shouldEqual maxDouble.length
    jsValue0 shouldEqual PlayJsonJsoniter.deserialize(maxDouble.getBytes).get
  }
}