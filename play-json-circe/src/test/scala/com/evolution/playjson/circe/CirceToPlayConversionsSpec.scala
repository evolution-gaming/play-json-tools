package com.evolution.playjson.circe

import com.evolution.playjson.circe.CirceToPlayConversions._
import io.circe._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.libs.json.{JsSuccess, Json}


class CirceToPlayConversionsSpec extends AnyFreeSpec with TableDrivenPropertyChecks with Matchers {
  case class Test(value: Int)

  "Circe to Play conversions" - {
    "given circe Decoder can derive play-json Reads" in {
      implicit val decoder: Decoder[Test] = Decoder.forProduct1("value")(Test.apply)
      assert(Json.parse("""{"value":42}""").validate[Test] == JsSuccess(Test(42)))
    }

    "given circe Encoder can derive play-json Writes" in {
      implicit val encoder: Encoder[Test] = Encoder.forProduct1("value")(_.value)
      assert(Json.toJson(Test(42)) === Json.obj("value"-> 42))
    }
  }
}
