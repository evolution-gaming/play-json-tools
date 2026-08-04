package com.evolution.playjson.generic

import play.api.libs.json._

/**
  * Contract tests for the discriminators of [[NestedTypeFormat]] and the labels of [[Enumeration]],
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
      implicit val commandFormat: OFormat[Command] = NestedTypeFormat[Command]

      check[Command](Command.Stop, Json.obj("type" -> "Stop"))
    }
  }

  "Enumeration" should {

    "refuse labels that collide once the naming strategy is applied" in {
      // a strategy that loses information, which is all it takes for two labels to become one
      implicit val firstThreeLetters: NameCodingStrategy = new NameCodingStrategy {
        def apply(name: String): String = name.take(3)
      }

      val failure = the[IllegalArgumentException] thrownBy Enumeration[Colour].format

      failure.getMessage should include("Red")
    }
  }
}
