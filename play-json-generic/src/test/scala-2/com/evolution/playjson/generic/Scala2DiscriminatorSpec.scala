package com.evolution.playjson.generic

import play.api.libs.json._

/**
  * What [[NestedTypeFormat]] does on Scala 2 and cannot do the same way on Scala 3: the name comes
  * from lexical nesting, so an object between the sealed trait and its subtype becomes part of it,
  * and a subtype with nothing above it is left with an empty name.
  */
class Scala2DiscriminatorSpec extends JsonFormatSpec {

  "NestedTypeFormat on Scala 2" should {

    "take the name from lexical nesting" in {
      implicit val leafFormat: OFormat[Wrapper.Inner.Leaf] = Json.format[Wrapper.Inner.Leaf]
      implicit val wrapperFormat: OFormat[Wrapper] = formatOf(NestedTypeFormat.of[Wrapper])

      check[Wrapper](Wrapper.Inner.Leaf(1), Json.obj("type" -> "Inner.Leaf", "value" -> 1))
    }

    /**
      * An empty name is unhelpful but unambiguous while it belongs to the only subtype, and it is
      * what earlier versions wrote, so documents holding it have to keep being readable.
      */
    "write a lone subtype declared at the top level with an empty name" in {
      implicit val pingFormat: OFormat[Ping] = Json.format[Ping]
      implicit val signalFormat: OFormat[Signal] = formatOf(NestedTypeFormat.of[Signal])

      check[Signal](Ping(1), Json.obj("type" -> "", "id" -> 1))
    }

    "write a lone package-level object with an empty name" in {
      implicit val pulseFormat: OFormat[Pulse.type] = new OFormat[Pulse.type] {
        def writes(o: Pulse.type): JsObject = Json.obj()
        def reads(json: JsValue): JsResult[Pulse.type] = JsSuccess(Pulse)
      }
      implicit val beaconFormat: OFormat[Beacon] = formatOf(NestedTypeFormat.of[Beacon])

      check[Beacon](Pulse, Json.obj("type" -> ""))
    }

    "report several subtypes declared at the top level" in {
      implicit val openFormat: OFormat[Open] = Json.format[Open]
      implicit val closeFormat: OFormat[Close] = Json.format[Close]

      NestedTypeFormat.of[Relay] match {
        case Left(error)   => error should (include("Open") and include("Close") and include("empty name"))
        case Right(format) => fail(s"expected subtypes sharing one name, got $format")
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
