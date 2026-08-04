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

    /**
      * The name of a package-level plain object still carries its package, which is what earlier
      * versions wrote. Pinned rather than corrected: documents holding the long form have to keep
      * being readable.
      */
    "keep the package in the name of a package-level object" in {
      given OFormat[Pulse.type] = new OFormat[Pulse.type] {
        def writes(o: Pulse.type): JsObject = Json.obj()
        def reads(json: JsValue): JsResult[Pulse.type] = JsSuccess(Pulse)
      }
      given OFormat[Beacon] = formatOf(NestedTypeFormat.of[Beacon])

      check[Beacon](Pulse, Json.obj("type" -> "com.evolution.playjson.generic.Pulse"))
    }

    /**
      * Where the package-level shape and the `toString` override meet. This one used to fail
      * outright: the override left nothing to drop, and the name was read off an empty array.
      */
    "name a package-level object that overrides toString" in {
      given OFormat[Chime.type] = new OFormat[Chime.type] {
        def writes(o: Chime.type): JsObject = Json.obj()
        def reads(json: JsValue): JsResult[Chime.type] = JsSuccess(Chime)
      }
      given OFormat[Alarm] = formatOf(NestedTypeFormat.of[Alarm])

      check[Alarm](Chime, Json.obj("type" -> "com.evolution.playjson.generic.Chime"))
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
