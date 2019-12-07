package com.evolutiongaming.playjsontools

import com.evolutiongaming.playjsontools.PlayJsonHelper._
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class FoldedTypeFormatSpec extends AnyWordSpec with Matchers {
  import FoldedTypeFormatSpec._

  "FoldedTypeFormat" should {

    "read and write json" in {
      implicit val format = GenericFoldedTypeFormat

      for {
        (o, json) <- Seq(
          GenericClass(42) -> genericClassJson,
          GenericObj -> genericObjJson,
          SpecificClass("test") -> specificClassJson,
          SpecificObj -> specificObjJson
        )
      } yield check[Generic](o, json)
    }

    "fail to convert to json in case of nested TypeFormat-s" in {
      val e = intercept[RuntimeException] {
        GenericTypeFormat.writes(SpecificObj)
      }
      e.getMessage shouldBe "Inner JSON for 'Specific' subtype already contains 'type' field"
    }

    def check[T](o: T, json: JsObject)(implicit format: Format[T]): Assertion = {
      withClue(s"writing $o: ") { format.writes(o) shouldEqual json }
      withClue(s"reading $o: ") { format.reads(json) shouldEqual JsSuccess(o) }
    }
  }

  val specificObjJson = Json.obj("type" -> "Specific#SpecificObj")
  val specificClassJson = Json.obj("type" -> "Specific#SpecificClass", "value" -> "test")
  val genericObjJson = Json.obj("type" -> "GenericObj")
  val genericClassJson = Json.obj("type" -> "GenericClass", "value" -> 42)

  val genericObjFormat = OFormat.const(GenericObj)
  val genericClassFormat = Json.format[GenericClass]
  val specificObjFormat = OFormat.const(SpecificObj)
  val specificClassFormat = Json.format[SpecificClass]

  val SpecificFormat: OFormat[Specific] = new TypeFormat[Specific] {
    def readsPf(json: JsValue): Pf = {
      case "SpecificObj"   => specificObjFormat.reads(json)
      case "SpecificClass" => specificClassFormat.reads(json)
    }
    def writes(x: Specific): JsObject = x match {
      case SpecificObj      => writes("SpecificObj", specificObjFormat.writes(SpecificObj))
      case x: SpecificClass => writes("SpecificClass", specificClassFormat.writes(x))
    }
  }

  val GenericTypeFormat: OFormat[Generic] = new TypeFormat[Generic] {
    def readsPf(json: JsValue): Pf = {
      case "GenericClass" => genericClassFormat.reads(json)
      case "GenericObj"   => genericObjFormat.reads(json)
      case "Specific"     => SpecificFormat.reads(json)
    }
    def writes(x: Generic): JsObject = x match {
      case x: GenericClass => writes("GenericClass", genericClassFormat.writes(x))
      case GenericObj      => writes("GenericObj", genericObjFormat.writes(GenericObj))
      case x: Specific     => writes("Specific", SpecificFormat.writes(x))
    }
  }

  val GenericFoldedTypeFormat: OFormat[Generic] = OFormat(
    FoldedTypeFormat.reads[Generic](json => {
      case "GenericClass" => genericClassFormat.reads(json)
      case "GenericObj"   => genericObjFormat.reads(json)
      case "Specific"     => SpecificFormat.reads(json)
    }),
    FoldedTypeFormat.writes[Generic] {
      case GenericObj      => ("GenericObj", genericObjFormat.writes(GenericObj))
      case x: GenericClass => ("GenericClass", genericClassFormat.writes(x))
      case x: Specific     => ("Specific", SpecificFormat.writes(x))
    })
}

object FoldedTypeFormatSpec {
  sealed trait Generic
  final case class GenericClass(value: Int) extends Generic
  case object GenericObj extends Generic
  sealed trait Specific extends Generic
  case object SpecificObj extends Specific
  final case class SpecificClass(value: String) extends Specific
}
