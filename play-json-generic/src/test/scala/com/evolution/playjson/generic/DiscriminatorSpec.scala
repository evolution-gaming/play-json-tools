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
