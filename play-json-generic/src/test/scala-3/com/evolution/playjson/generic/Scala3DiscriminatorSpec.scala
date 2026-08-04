package com.evolution.playjson.generic

import play.api.libs.json._

/**
  * What [[NestedTypeFormat]] does on Scala 3 and cannot do the same way on Scala 2: the name comes
  * from the sealed hierarchy, so a plain object around a subtype contributes nothing to it. A
  * subtype declared at the top level is named like any other, and two subtypes of the same name in
  * different objects end up sharing one.
  */
class Scala3DiscriminatorSpec extends JsonFormatSpec {

  "NestedTypeFormat on Scala 3" should {

    "take the name from the sealed hierarchy" in {
      given OFormat[Wrapper.Inner.Leaf] = Json.format[Wrapper.Inner.Leaf]
      given OFormat[Wrapper] = formatOf(NestedTypeFormat.of[Wrapper])

      check[Wrapper](Wrapper.Inner.Leaf(1), Json.obj("type" -> "Leaf", "value" -> 1))
    }

    "accept a subtype declared at the top level" in {
      given OFormat[Ping] = Json.format[Ping]
      given OFormat[Signal] = formatOf(NestedTypeFormat.of[Signal])

      check[Signal](Ping(1), Json.obj("type" -> "Ping", "id" -> 1))
    }

    "report subtypes of the same name in different objects" in {
      given firstFormat: OFormat[Tree.First.Node] = Json.format[Tree.First.Node]
      given secondFormat: OFormat[Tree.Second.Node] = Json.format[Tree.Second.Node]

      NestedTypeFormat.of[Tree] match {
        case Left(error)   => error should include("Node")
        case Right(format) => fail(s"expected one name for both subtypes, got $format")
      }
    }
  }
}
