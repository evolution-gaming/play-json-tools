package com.evolution.playjson.generic

import play.api.libs.json._

/**
  * Contract tests for the discriminators of [[NestedTypeFormat]] and the labels of [[EnumerationFormat]],
  * covering what both Scala versions have to agree on. The parts where they differ are pinned in
  * `src/test/scala-2` and `src/test/scala-3`.
  */
class DiscriminatorSpec extends JsonFormatSpec {

  "NestedTypeFormat" should {

    "name a subtype after its type, not after its toString" in {
      implicit val stopFormat: OFormat[Command.Stop.type] = new OFormat[Command.Stop.type] {
        def writes(o: Command.Stop.type): JsObject = Json.obj()
        def reads(json: JsValue): JsResult[Command.Stop.type] = JsSuccess(Command.Stop)
      }
      implicit val commandFormat: OFormat[Command] = formatOf(NestedTypeFormat.of[Command])

      check[Command](Command.Stop, Json.obj("type" -> "Stop"))
    }

    /**
      * The names validation sees come from their own traversal of the hierarchy, separate from the
      * one the writer uses. This pins the two together: `NestedTypeFormatSpec` fixes the same four
      * names as the wire format, so one traversal drifting from the other fails one of the two.
      */
    "name the subtypes it writes" in {
      Discriminators[Message].all.map(_.name).toSet shouldEqual
        Set("Noop", "In.Update", "Out.Updated", "Out.Ack")
    }
  }

  "FlatTypeFormat" should {

    /**
      * IGNORED, fails today. Naming by the subtype alone collides on more hierarchies than
      * [[NestedTypeFormat]] does, and nothing reports it: both subtypes write `{"type":"Update"}`,
      * and reading the second back gives `JsError(/a, error.path.missing)`, because it is read
      * against the fields of the first.
      *
      * Enable once `FlatTypeFormat` either distinguishes such subtypes or gains an `of` refusing
      * them, as [[NestedTypeFormat.of]] does.
      */
    "read back a subtype whose simple name another subtype shares" ignore {
      implicit val inFormat: OFormat[Duct.In.Update] = Json.format[Duct.In.Update]
      implicit val outFormat: OFormat[Duct.Out.Update] = Json.format[Duct.Out.Update]
      val format = FlatTypeFormat[Duct]

      format.reads(format.writes(Duct.Out.Update(2))) shouldEqual JsSuccess(Duct.Out.Update(2))
    }
  }

  "EnumerationFormat" should {

    "report labels that collide once the naming strategy is applied" in {
      // a strategy that loses information, which is all it takes for two labels to become one
      implicit val firstThreeLetters: NameCodingStrategy = new NameCodingStrategy {
        def apply(name: String): String = name.take(3)
      }

      EnumerationFormat.of[Colour] match {
        case Left(error)   => error should include("Red")
        case Right(format) => fail(s"expected colliding labels, got $format")
      }
    }

    "give a format for labels that do not collide" in {
      EnumerationFormat.of[Colour] match {
        case Right(format) => format.writes(Colour.RedLight) shouldEqual JsString("RedLight")
        case Left(error)   => fail(error)
      }
    }
  }
}
