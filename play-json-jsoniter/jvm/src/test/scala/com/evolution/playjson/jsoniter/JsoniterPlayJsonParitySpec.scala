package com.evolution.playjson.jsoniter

import TestData.DataLine
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsSuccess, Json}

import java.nio.charset.StandardCharsets

/**
 * Covers the direction [[JsoniterSpec]] does not: documents written by this codec and read by
 * play-json.
 *
 * JVM only on purpose. play-json's Scala.js backend writes numbers as `toString`, without the
 * trailing-zero stripping and plain-range handling of its JVM serializer, so byte parity is a
 * claim about the JVM alone.
 */
class JsoniterPlayJsonParitySpec extends AnyFunSuite with Matchers {

  private def dataLine: DataLine =
    Json
      .fromJson[DataLine](Json.parse(TestData.jsonBody))
      .fold(errs => throw new Exception(s"Parsing error: ${errs.mkString(",")}"), identity)

  test("Write using Jsoniter and PlayJson: Compare bytes") {
    val jsValue = Json.toJson(dataLine)
    val bts = PlayJsonJsoniter.serialize(jsValue)

    new String(bts, StandardCharsets.UTF_8) shouldEqual new String(Json.toBytes(jsValue), StandardCharsets.UTF_8)
  }

  test("Write using Jsoniter -> Read using PlayJson: Compare objects") {
    val expected = dataLine
    val bts = PlayJsonJsoniter.serialize(Json.toJson(expected))

    Json.fromJson[DataLine](Json.parse(bts)) shouldEqual JsSuccess(expected)
  }
}
