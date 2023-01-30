package com.evolutiongaming.util

import io.circe
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.{json => play}
import org.scalatest.prop.TableDrivenPropertyChecks


class PlayCirceAstConversionsSpec extends AnyFreeSpec with TableDrivenPropertyChecks with Matchers {

  "Play to/from Circe AST conversions" in {
    forAll {
      Table[play.JsValue, circe.Json](
        ("Play JSON", "Circe JSON"),
        (play.JsNull, circe.Json.Null),
        (play.JsTrue, circe.Json.True),
        (play.JsFalse, circe.Json.False),
        (play.JsString("play <-> circe"), circe.Json.fromString("play <-> circe")),
        (play.JsNumber(2.71828), circe.Json.fromBigDecimal(2.71828)),
        (
          play.Json.arr(play.JsNull, play.JsTrue, play.JsString("something")),
          circe.Json.arr(circe.Json.Null, circe.Json.True, circe.Json.fromString("something"))
        ),
        (
          play.Json.obj("inner" -> play.Json.obj("null" -> play.JsNull)),
          circe.Json.obj("inner" -> circe.Json.obj("null" -> circe.Json.Null))
        ),
      )
    } { (playAst, circeAst) =>
      assert(PlayCirceAstConversions.playToCirce(playAst) == circeAst, "[from play to circe]")
      assert(PlayCirceAstConversions.circeToPlay(circeAst) == playAst, "[from circe to play]")
    }

  }


}