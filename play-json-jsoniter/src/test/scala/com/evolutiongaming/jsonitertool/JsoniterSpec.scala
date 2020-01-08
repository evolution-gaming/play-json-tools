package com.evolutiongaming.jsonitertool

import com.evolutiongaming.jsonitertool.TestDataGenerators.DataLine
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsSuccess, Json}
import TestDataGenerators._

class JsoniterSpec extends AnyFunSuite with Matchers {

  test("Write using PlayJson -> Read using Jsoniter: Compare bytes") {

    val expected: DataLine = Json.fromJson[DataLine](Json.parse(TestDataGenerators.jsonBody))
      .fold(errs => throw new Exception(s"Parsing error: ${errs.mkString(",")}"), identity)

    val jsValue = Json.toJson(expected)
    val bts0 = PlayJsonJsoniter.serialize(jsValue)
    val bts1 = Json.toBytes(jsValue)
    java.util.Arrays.compare(bts0, bts1) shouldEqual 0

  }

  test("Write using PlayJson -> Read using Jsoniter: Compare objects") {

    val expected: DataLine = Json.fromJson[DataLine](Json.parse(TestDataGenerators.jsonBody))
      .fold(errs => throw new Exception(s"Parsing error: ${errs.mkString(",")}"), identity)

    val bts = Json.toBytes(Json.toJson(expected))
    val jsValue = PlayJsonJsoniter.deserialize(bts)
    val actual = Json.fromJson[DataLine](jsValue)

    JsSuccess(expected) shouldEqual actual
  }
}