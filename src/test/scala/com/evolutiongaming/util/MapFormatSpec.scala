package com.evolutiongaming.util

import com.evolutiongaming.util.JsonFormats._
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsSuccess, Json}

class MapFormatSpec extends WordSpec with Matchers {

  "MapFormat" should {
    
    implicit val valueFormat = Json.format[Value]
    implicit val keyFormat = Json.format[Key]

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

  case class Key(value: String)
  case class Value(value: Int)
}
