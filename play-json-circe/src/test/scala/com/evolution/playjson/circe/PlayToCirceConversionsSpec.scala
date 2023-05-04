package com.evolution.playjson.circe

import com.evolution.playjson.circe.PlayToCirceConversions._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.libs.json.{Json, Reads, Writes}


class PlayToCirceConversionsSpec extends AnyFreeSpec with TableDrivenPropertyChecks with Matchers {
  case class Test(value: Option[Int])

  "Play to Circe conversions" - {
    "given play-json Reads can derive circe decoder" in {
      implicit val reads: Reads[Test] = Json.format[Test] // used by decode
      assert(decode[Test]("""{"value":42}""") == Right(Test(Some(42))), "with value")
      assert(decode[Test]("""{"value":null}""") == Right(Test(None)), "with no value as null")
      assert(decode[Test]("""{}""") == Right(Test(None)), "with no value")
    }

    "given play-json Writes can derive circe encoder" in {
      implicit val writes: Writes[Test] = Json.format[Test] // used by asJson
      assert(Test(Some(42)).asJson.noSpaces === """{"value":42}""")
      assert(Test(None).asJson.noSpaces === """{}""")
    }
  }
}
