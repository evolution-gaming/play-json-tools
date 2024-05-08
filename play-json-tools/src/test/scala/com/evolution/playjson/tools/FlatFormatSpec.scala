package com.evolution.playjson.tools

import com.evolution.playjson.tools.FlatFormatSpec.{Inner, Outer}
import com.evolution.playjson.tools.PlayJsonHelper.FlatFormat
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, Json}

import scala.reflect.ClassTag


class FlatFormatSpec extends AnyFunSuite with Matchers {
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

  test("throw an exception if trying to create with non-existing field") {
    assertThrows[NoSuchFieldException] {
      implicitly[ClassTag[Outer]].runtimeClass.getDeclaredField("inner2")
    }
    assertThrows[IllegalArgumentException] {
      FlatFormat("inner2", Json.format[Outer])
    }
  }
}

object FlatFormatSpec {
  case class Inner(inner: String)

  object Inner {
    implicit val JsonFormat: Format[Inner] = Json.format[Inner]
  }

  case class Outer(outer: String, inner: Inner)

  object Outer {
    implicit val JsonFormat: Format[Outer] = FlatFormat("inner", Json.format[Outer])
  }
}