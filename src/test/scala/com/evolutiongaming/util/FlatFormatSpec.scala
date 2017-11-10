package com.evolutiongaming.util

import com.evolutiongaming.util.JsonFormats.FlatFormat
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.{Format, Json}


class FlatFormatSpec extends FunSuite with Matchers {
  private val json = Json.obj(
    "outer" -> "outer",
    "inner" -> "inner")
  private val outer = Outer("outer", Inner("inner"))

  test("read flat json") {
    Json.fromJson[Outer](json).get shouldEqual outer
  }

  test("write flat json") {
    Json.toJson(outer) shouldEqual json
  }

  case class Inner(inner: String)
  object Inner {
    implicit val JsonFormat: Format[Inner] = Json.format[Inner]
  }

  case class Outer(outer: String, inner: Inner)
  object Outer {
    implicit val JsonFormat: Format[Outer] = new FlatFormat[Outer]("inner", Json.format[Outer])
  }
}