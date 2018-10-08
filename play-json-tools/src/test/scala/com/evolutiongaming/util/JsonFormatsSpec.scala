package com.evolutiongaming.util

import com.evolutiongaming.nel.Nel
import com.evolutiongaming.util.JsonFormats._
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json._

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

  test("unitFormat") {
    val value = ()
    val json = Json.toJson(value)
    json shouldEqual JsNull
    Json.fromJson[Unit](json) shouldEqual JsSuccess(value)
  }

  test("constFormat") {
    val format = const(ConstObject)
    val value = ConstObject
    val json = Json.obj()
    format.writes(value) shouldEqual json
    format.reads(json) shouldEqual JsSuccess(value)
  }

  test("nestedFormat") {
    val format = nested[Data]("nestedData")
    val data = Data(123)
    val json = Json.obj("nestedData" -> Json.obj("value" -> 123))
    format.writes(data) shouldEqual json
    format.reads(json) shouldEqual JsSuccess(data)
  }

  case object ConstObject
  case class Data(value: Int)
  implicit val dataFormat: OFormat[Data] = Json.format[Data]
}
