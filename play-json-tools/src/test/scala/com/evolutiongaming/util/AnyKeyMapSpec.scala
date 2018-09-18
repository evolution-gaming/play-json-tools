package com.evolutiongaming.util

import com.evolutiongaming.util.JsonFormats.AnyKeyMap
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsError, JsSuccess, Json}

  class AnyKeyMapSpec extends WordSpec with Matchers {

  "AnyKeyMap" should {

    val json = Json.obj(
      "42" -> Json.obj("value" -> 1),
      "1" -> Json.obj("value" -> 42))

    implicit val entryFormat = Json.format[Value]
    val mapFormat = new AnyKeyMap[Key, Value](_.id.toString, Key(_))

    val map = Map(Key(42) -> Value(1), Key(1) -> Value(42))

    "convert to json" in {
      mapFormat.writes(map) shouldEqual json
    }

    "parse from json" in {
      mapFormat.reads(json) shouldEqual JsSuccess(map)
    }

    "fail to parse invalid json" in {
      val invalidJson = json + ("foo" -> Json.obj("value" -> 10))
      mapFormat.reads(invalidJson) shouldBe JsError(s"cannot parse key from foo")
    }
  }

  case class Value(value: Int)

  case class Key(id: Int)

  object Key {
    def apply(id: String): Key = Key(id.toInt)
  }
}