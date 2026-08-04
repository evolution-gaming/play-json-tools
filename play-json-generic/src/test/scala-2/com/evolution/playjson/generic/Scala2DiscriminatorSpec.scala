package com.evolution.playjson.generic

import play.api.libs.json._

/**
  * What [[NestedTypeFormat]] does on Scala 2 and cannot do the same way on Scala 3: the
  * discriminator comes from lexical nesting, so an object between the sealed trait and its subtype
  * becomes part of the name, and a subtype with nothing above it has no name left to give.
  */
class Scala2DiscriminatorSpec extends JsonFormatSpec {

  "NestedTypeFormat on Scala 2" should {

    "take the discriminator from lexical nesting" in {
      implicit val leafFormat: OFormat[Wrapper.Inner.Leaf] = Json.format[Wrapper.Inner.Leaf]
      implicit val wrapperFormat: OFormat[Wrapper] = NestedTypeFormat[Wrapper]

      check[Wrapper](Wrapper.Inner.Leaf(1), Json.obj("type" -> "Inner.Leaf", "value" -> 1))
    }

    "refuse a subtype declared at the top level" in {
      implicit val pingFormat: OFormat[Ping] = Json.format[Ping]

      val failure = the[IllegalArgumentException] thrownBy NestedTypeFormat[Signal]

      failure.getMessage should include("Ping")
    }
  }
}
