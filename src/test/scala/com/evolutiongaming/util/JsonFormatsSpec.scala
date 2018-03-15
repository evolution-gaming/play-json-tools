package com.evolutiongaming.util

import com.evolutiongaming.util.JsonFormats._
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.{JsNumber, JsSuccess, Json}

class JsonFormatsSpec extends FunSuite with Matchers {

  test("nelFormat") {
    val value = Nel(1, 2)
    val json = Json.toJson(value)
    json shouldEqual Json.arr(1, 2)
    Json.fromJson[Nel[Int]](json) shouldEqual JsSuccess(value)
  }

  test("eitherFormat left") {
    val value: Either[String, Int] = Left("1")
    val json = Json.toJson(value)
    json shouldEqual Json.obj("left" -> "1")
    Json.fromJson[Either[String, Int]](json) shouldEqual JsSuccess(value)
  }

  test("eitherFormat right") {
    val value: Either[String, Int] = Right(2)
    val json = Json.toJson(value)
    json shouldEqual JsNumber(2)
    Json.fromJson[Either[String, Int]](json) shouldEqual JsSuccess(value)
  }
}
