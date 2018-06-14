package com.evolutiongaming.util.generic

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{Json, Reads, Writes}

class EnumerationDerivalSpec extends FlatSpec with Matchers {

  it should "be able to encode and decode case object enumerations using default low prio implicit" in {
    import EnumerationDerivalSpec.Formats.Default._

    val typ: AnEvent = AnEvent.DoneSome
    val js = Json.toJson(typ)
    js.toString() shouldBe "\"DoneSome\""
    js.as[AnEvent] shouldBe typ
  }

  it should "be able to encode and decode in kebab case" in {
    import EnumerationDerivalSpec.Formats.Kebab._

    val typ: AnEvent = AnEvent.DoneSome
    val json = Json.toJson(typ)

    json.toString() shouldBe "\"done-some\""
    json.as[AnEvent] shouldBe typ
    succeed
  }

  it should "be able to derive formats" in {
    implicit val fmt = EnumerationFormats[AnEvent]

    val typ: AnEvent = AnEvent.DoneSome
    val js = Json.toJson(typ)
    js.toString() shouldBe "\"DoneSome\""
    js.as[AnEvent] shouldBe typ
  }

}

object EnumerationDerivalSpec {
  object Formats {
    object Kebab {
      import NameCodingStrategies.kebabCase
      implicit val aReads: Reads[AnEvent] = EnumerationReads[AnEvent]
      implicit val aWrites: Writes[AnEvent] = EnumerationWrites[AnEvent]
    }
    object Default {
      implicit val aReads: Reads[AnEvent] = EnumerationReads[AnEvent]
      implicit val aWrites: Writes[AnEvent] = EnumerationWrites[AnEvent]
    }
  }
}