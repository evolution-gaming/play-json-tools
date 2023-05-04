package com.evolution.playjson.tools

import com.evolution.playjson.tools.PlayJsonHelper._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

class DiscriminatedEitherFormatSpec extends AnyFunSuite with Matchers {
  implicit val eitherFormat = DiscriminatedEitherFormat.eitherFormat("L", "R"): OFormat[Either[String, Int]]
  val left: Either[String, Int] = Left("foo")
  val right: Either[String, Int] = Right(1)
  val fromEitherJson: JsValue => JsResult[Either[String, Int]] = Json.fromJson[Either[String, Int]](_)

  test("eitherFormat left") {
    val value = left
    val json = Json.toJson(value)
    json shouldEqual Json.obj("L" -> "foo")
    fromEitherJson(json) shouldEqual JsSuccess(value)
  }

  test("eitherFormat right") {
    val value = right
    val json = Json.toJson(value)
    json shouldEqual Json.obj("R" -> 1)
    fromEitherJson(json) shouldEqual JsSuccess(value)
  }

  test("eitherFormat should fail empty objects") {
    fromEitherJson(JsObject.empty) shouldEqual JsError("no discriminator keys found")
  }

  test("eitherFormat should fail on non-objects") {
    fromEitherJson(JsNull) shouldEqual JsError("can't parse either for non-object for null")
  }

  test("eitherFormat should fail when both L/R are present") {
    fromEitherJson(Json.obj("R" -> 1, "L" -> "foo")) shouldEqual JsError("""both discriminator keys received""")
  }

  test("eitherFormat should succeed on extra keys") {
    fromEitherJson(Json.obj("R" -> 1, "X" -> JsNull)) shouldEqual JsSuccess(right)
  }
}
