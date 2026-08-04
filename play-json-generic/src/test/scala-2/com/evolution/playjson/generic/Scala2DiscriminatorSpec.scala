package com.evolution.playjson.generic

import play.api.libs.json._

/**
  * What [[NestedTypeFormat]] does on Scala 2 and cannot do the same way on Scala 3: the name comes
  * from lexical nesting, so an object between the sealed trait and its subtype becomes part of it,
  * and a subtype with nothing above it has no name left to give.
  */
class Scala2DiscriminatorSpec extends JsonFormatSpec {

  "NestedTypeFormat on Scala 2" should {

    "take the name from lexical nesting" in {
      implicit val leafFormat: OFormat[Wrapper.Inner.Leaf] = Json.format[Wrapper.Inner.Leaf]
      implicit val wrapperFormat: OFormat[Wrapper] = formatOf(NestedTypeFormat.of[Wrapper])

      check[Wrapper](Wrapper.Inner.Leaf(1), Json.obj("type" -> "Inner.Leaf", "value" -> 1))
    }

    "report a subtype declared at the top level" in {
      implicit val pingFormat: OFormat[Ping] = Json.format[Ping]

      NestedTypeFormat.of[Signal] match {
        case Left(error)   => error should include("Ping")
        case Right(format) => fail(s"expected an unnamed subtype, got $format")
      }
    }

    "report a plain object declared at package level" in {
      implicit val pulseFormat: OFormat[Pulse.type] = new OFormat[Pulse.type] {
        def writes(o: Pulse.type): JsObject = Json.obj()
        def reads(json: JsValue): JsResult[Pulse.type] = JsSuccess(Pulse)
      }

      NestedTypeFormat.of[Beacon] match {
        case Left(error)   => error should include("Pulse")
        case Right(format) => fail(s"expected an unnamed subtype, got $format")
      }
    }

    "keep subtypes of the same name apart by the object holding them" in {
      implicit val firstFormat: OFormat[Tree.First.Node] = Json.format[Tree.First.Node]
      implicit val secondFormat: OFormat[Tree.Second.Node] = Json.format[Tree.Second.Node]
      implicit val treeFormat: OFormat[Tree] = formatOf(NestedTypeFormat.of[Tree])

      check[Tree](Tree.First.Node(1), Json.obj("type" -> "First.Node", "value" -> 1))
      check[Tree](Tree.Second.Node(2), Json.obj("type" -> "Second.Node", "value" -> 2))
    }
  }
}
