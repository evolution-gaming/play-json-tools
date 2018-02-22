package com.evolutiongaming.util

import com.evolutiongaming.util.JsonFormats._
import com.evolutiongaming.util.MapFormatSpec._
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsSuccess, Json, OFormat}

class MapFormatSpec extends WordSpec with Matchers {

  "MapFormat" should {

    val mapFormat = new MapFormat[Key, Value]("id")

    val map = Map(
      (Key("k1"), Value(1)),
      (Key("k2"), Value(2)),
      (Key("k3"), Value(3)))

    val json = Json.arr(
      Json.obj(
        "id" -> Json.obj("value" -> "k1"),
        "value" -> 1),
      Json.obj(
        "id" -> Json.obj("value" -> "k2"),
        "value" -> 2),
      Json.obj(
        "id" -> Json.obj("value" -> "k3"),
        "value" -> 3))

    "convert to json" in {
      mapFormat.writes(map) shouldEqual json
    }

    "parse from json" in {
      mapFormat.reads(json) shouldEqual JsSuccess(map)
    }

  }

  "MapFormat primitive key" should {

    val mapFormat = new MapFormat[String, Int]("id")

    val map = Map(
      ("k1", 1),
      ("k2", 2),
      ("k3", 3))

    val json = Json.arr(
      Json.obj(
        "id" -> "k1",
        "value" -> 1),
      Json.obj(
        "id" -> "k2",
        "value" -> 2),
      Json.obj(
        "id" -> "k3",
        "value" -> 3))

    "convert to json" in {
      mapFormat.writes(map) shouldEqual json
    }

    "parse from json" in {
      mapFormat.reads(json) shouldEqual JsSuccess(map)
    }
  }

  "MapFormat's key" should {

    "not be named after a field of the value" in {
      an[IllegalArgumentException] should be thrownBy {
        new MapFormat[ComboKey, RichValue]("key")
      }
    }

    "be present twice in result JSON if it's part of the value" in {
      val mapFormat = new MapFormat[ComboKey, RichValue]("id")

      val value1 = RichValue(ComboKey("k1_1", "k1_2"), Value(42))

      val map: Map[ComboKey, RichValue] = Map((value1.key, value1))

      val duplicatedKey = Json.obj("k1" -> "k1_1", "k2" -> "k1_2")
      val json = Json.arr(
        Json.obj(
          "id" -> duplicatedKey,
          "key" -> duplicatedKey,
          "value" -> Json.obj("value" -> 42)
        )
      )

      mapFormat.writes(map) shouldBe json
      mapFormat.reads(json) shouldBe JsSuccess(map)
    }

  }

}

object MapFormatSpec {
  case class Key(value: String)
  case class Value(value: Int)

  case class ComboKey(k1: String, k2: String)
  case class RichValue(key: ComboKey, value: Value)

  implicit val keyFormat: OFormat[Key] = Json.format[Key]
  implicit val valueFormat: OFormat[Value] = Json.format[Value]

  implicit val comboKeyFormat: OFormat[ComboKey] = Json.format[ComboKey]
  implicit val richValueFormat: OFormat[RichValue] = Json.format[RichValue]
}