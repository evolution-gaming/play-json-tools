package com.evolutiongaming.util

import com.evolutiongaming.util.JsonFormats.{FoldedTypeFormat, TypeFormat}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json._

class FoldedTypeFormatSpec extends WordSpec with Matchers {

  "FoldedTypeFormat" should {

    "convert to json" in {
      GenericFoldedTypeFormat.writes(SpecificObj) shouldEqual json
    }

    "parse from json" in {
      GenericFoldedTypeFormat.reads(json) shouldEqual JsSuccess(SpecificObj)
    }

    "fail to convert to json in case of nested TypeFormat-s" in {
      val e = intercept[RuntimeException] {
        GenericTypeFormat.writes(SpecificObj)
      }
      e.getMessage shouldBe "Inner JSON for 'Specific' subtype already contains 'type' field"
    }
  }

  val json = Json.obj("type" -> "Specific#SpecificObj")

  sealed trait Generic
  sealed trait Specific extends Generic
  case object SpecificObj extends Specific

  val SpecificFormat: OFormat[Specific] = new TypeFormat[Specific] {
    def readsPf(json: JsValue): Pf = {
      case "SpecificObj" => JsSuccess(SpecificObj)
    }
    def writes(x: Specific): JsObject = x match {
      case SpecificObj => writes("SpecificObj")
    }
  }

  val GenericTypeFormat: OFormat[Generic] = new TypeFormat[Generic] {
    def readsPf(json: JsValue): Pf = {
      case "Specific" => SpecificFormat.reads(json)
    }
    def writes(x: Generic): JsObject = x match {
      case x: Specific => writes("Specific", SpecificFormat.writes(x))
    }
  }

  val GenericFoldedTypeFormat: OFormat[Generic] = OFormat(
    FoldedTypeFormat.reads[Generic](json => {
      case "Specific" => SpecificFormat.reads(json)
    }),
    FoldedTypeFormat.writes[Generic]({
      case x: Specific => ("Specific", SpecificFormat.writes(x))
    }))
}
