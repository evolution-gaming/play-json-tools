package com.evolution.playjson.tools

import com.evolution.playjson.tools.PlayJsonHelper._
import com.evolutiongaming.nel.Nel
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

import java.time.{Instant, LocalTime}
import scala.concurrent.duration._

class PlayJsonHelperSpec extends AnyFunSuite with Matchers {

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

  test("eitherFormat turns a Right that writes a left field into a Left") {
    val value: Either[String, Map[String, String]] = Right(Map("left" -> "1"))
    val json = Json.toJson(value)
    json shouldEqual Json.obj("left" -> "1")
    Json.fromJson[Either[String, Map[String, String]]](json) shouldEqual
      JsSuccess(Left("1"): Either[String, Map[String, String]])
  }

  test("unitFormat") {
    val value = ()
    val json = Json.toJson(value)
    json shouldEqual JsNull
    Json.fromJson[Unit](json) shouldEqual JsSuccess(value)
  }

  test("constFormat") {
    val format = OFormat.const(ConstObject)
    val value = ConstObject
    val json = Json.obj()
    format.writes(value) shouldEqual json
    format.reads(json) shouldEqual JsSuccess(value)
  }

  test("nestedFormat") {
    val format = OFormat.nested[Data]("nestedData")
    val data = Data(123)
    val json = Json.obj("nestedData" -> Json.obj("value" -> 123))
    format.writes(data) shouldEqual json
    format.reads(json) shouldEqual JsSuccess(data)
  }

  test("finiteDurationFormat round-trips a duration") {
    val value = 1.minute
    val json = Json.toJson(value)
    json shouldEqual JsString("1 minute")
    Json.fromJson[FiniteDuration](json) shouldEqual JsSuccess(value)
  }

  test("finiteDurationFormat reads a number as milliseconds") {
    Json.fromJson[FiniteDuration](JsNumber(1500)) shouldEqual JsSuccess(1500.millis)
  }

  test("finiteDurationFormat reports a string it cannot parse") {
    Json.fromJson[FiniteDuration](JsString("garbage")) shouldBe a[JsError]
  }

  test("finiteDurationFormat reports a bad string as a string, not as a missing number") {
    errorMessagesOf(Json.fromJson[FiniteDuration](JsString("garbage"))) should include("garbage")
  }

  test("finiteDurationFormat reports a duration that is not finite") {
    Json.fromJson[FiniteDuration](JsString("Inf")) shouldBe a[JsError]
  }

  test("instantFormat round-trips, truncating to milliseconds") {
    val value = Instant.parse("2026-08-03T10:15:30.123456Z")
    val json = Json.toJson(value)
    json shouldEqual JsString("2026-08-03T10:15:30.123Z")
    Json.fromJson[Instant](json) shouldEqual JsSuccess(Instant.parse("2026-08-03T10:15:30.123Z"))
  }

  test("instantFormat reads an ISO-8601 instant") {
    Json.fromJson[Instant](JsString("2026-08-03T10:15:30Z")) shouldEqual
      JsSuccess(Instant.parse("2026-08-03T10:15:30Z"))
  }

  test("instantFormat reads a number as epoch milliseconds") {
    Json.fromJson[Instant](JsNumber(1785492930123L)) shouldEqual JsSuccess(Instant.ofEpochMilli(1785492930123L))
  }

  test("instantFormat reports a string it cannot parse") {
    Json.fromJson[Instant](JsString("garbage")) shouldBe a[JsError]
  }

  test("instantFormat reports a bad string as a string, not as a missing number") {
    errorMessagesOf(Json.fromJson[Instant](JsString("garbage"))) should include("garbage")
  }

  test("localTimeFormat round-trips") {
    val value = LocalTime.of(10, 15, 30)
    val json = Json.toJson(value)
    json shouldEqual JsString("10:15:30")
    Json.fromJson[LocalTime](json) shouldEqual JsSuccess(value)
  }

  test("localTimeFormat reports a string it cannot parse") {
    Json.fromJson[LocalTime](JsString("garbage")) shouldBe a[JsError]
  }

  private def errorMessagesOf(result: JsResult[Any]): String = result match {
    case JsError(errors) => errors.flatMap { case (_, invalid) => invalid.flatMap(_.messages) }.mkString(", ")
    case JsSuccess(value, _) => s"read successfully as $value"
  }

  case object ConstObject
  case class Data(value: Int)
  implicit val dataFormat: OFormat[Data] = Json.format[Data]
}
