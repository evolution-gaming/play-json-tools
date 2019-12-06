package com.evolutiongaming.util

import com.evolutiongaming.util.PlayJson27xCompat._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsString, Json, Writes}

class PlayJson27xCompatTest extends AnyFunSuite with Matchers {

  test("mapWrites") {

    def verify[A: Writes](a: A, expected: String) = {
      Json.toJson(a) shouldEqual Json.parse(expected)
    }

    verify(Map(("a", 0)), """{"a":0}""")
    verify(Map((JsString("a"), 0)), """[["a",0]]""")
    verify(Map((1, 0)), """[[1,0]]""")
  }
}
