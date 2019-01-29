package com.evolutiongaming.util

import com.evolutiongaming.util.JsonFormats.MapValuesFormat
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsSuccess, Json}

class MapValuesFormatSpec extends WordSpec with Matchers {

  "MapValuesFormat" should {
    implicit val entryFormat = Json.format[Entry]
    val mapFormat = MapValuesFormat[String, Entry](_.id)

    val values = List(
      Entry("k1", 1),
      Entry("k2", 2))

    val map = values.map { x => (x.id, x) }.toMap

    val json = Json.arr(
      Json.obj(
        "id" -> "k1",
        "value" -> 1),
      Json.obj(
        "id" -> "k2",
        "value" -> 2))

    "convert to json" in {
      mapFormat.writes(map) shouldEqual json
    }

    "parse from json" in {
      mapFormat.reads(json) shouldEqual JsSuccess(map)
    }
  }

  case class Entry(id: String, value: Int)
}
