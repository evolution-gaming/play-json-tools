package com.evolution.playjson.generic

import play.api.libs.json._

/**
  * What [[NestedTypeFormat]] does on Scala 3 and cannot do the same way on Scala 2: the
  * discriminator comes from the sealed hierarchy, so a plain object around a subtype contributes
  * nothing to the name, and a subtype declared at the top level is named like any other.
  */
class Scala3DiscriminatorSpec extends JsonFormatSpec {

  "NestedTypeFormat on Scala 3" should {

    "take the discriminator from the sealed hierarchy" in {
      given OFormat[Wrapper.Inner.Leaf] = Json.format[Wrapper.Inner.Leaf]

      check[Wrapper](Wrapper.Inner.Leaf(1), Json.obj("type" -> "Leaf", "value" -> 1))(NestedTypeFormat[Wrapper])
    }

    "accept a subtype declared at the top level" in {
      given OFormat[Ping] = Json.format[Ping]

      check[Signal](Ping(1), Json.obj("type" -> "Ping", "id" -> 1))(NestedTypeFormat[Signal])
    }
  }
}
