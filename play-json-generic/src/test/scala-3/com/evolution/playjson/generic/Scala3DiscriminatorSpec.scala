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

    "prefix with a sealed sub-trait declared at the top level" in {
      given OFormat[Branch.Leaf] = Json.format[Branch.Leaf]
      given OFormat[Root] = formatOf(NestedTypeFormat.of[Root])

      check[Root](Branch.Leaf(1), Json.obj("type" -> "Branch.Leaf", "value" -> 1))
    }

    "accept several subtypes declared at the top level" in {
      given openFormat: OFormat[Open] = Json.format[Open]
      given closeFormat: OFormat[Close] = Json.format[Close]
      given OFormat[Relay] = formatOf(NestedTypeFormat.of[Relay])

      check[Relay](Open(1), Json.obj("type" -> "Open", "id" -> 1))
      check[Relay](Close(2), Json.obj("type" -> "Close", "id" -> 2))
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

    /**
      * IGNORED, fails today. `EnumMappings` on Scala 3 labels a value by `e.toString`, so this one
      * is written as `in-a-good-mood` rather than `Cheerful`. Scala 2 labels it `Cheerful`, so the
      * two versions also disagree on the wire for any enumeration that overrides `toString`.
      *
      * Enable once the Scala 3 `EnumMappings` takes the label from the type, as `singletonName` now
      * does. That changes what Scala 3 writes for these enumerations, so it needs deciding on its
      * own rather than alongside a discriminator fix.
      */
    "label an enumeration value by its name, not by its toString" ignore {
      EnumerationFormat.of[Mood] match {
        case Right(format) => format.writes(Mood.Cheerful) shouldEqual JsString("Cheerful")
        case Left(error)   => fail(error)
      }
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
