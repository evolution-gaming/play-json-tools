package com.evolutiongaming.util.generic

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{Format, Json}

class EnumerationDerivalSpec extends FlatSpec with Matchers {

  it should "be able to encode and decode case object enumerations using default low prio implicit" in {
    implicit val format: Format[AnEvent] = EnumerationFormats[AnEvent]

    val typ: AnEvent = AnEvent.DoneSome
    val js = Json.toJson(typ)
    js.toString() shouldBe "\"DoneSome\""
    js.as[AnEvent] shouldBe typ
  }

  it should "be able to encode and decode in kebab case" in {
    import NameCodingStrategies.kebabCase
    implicit val format: Format[AnEvent] = EnumerationFormats[AnEvent]

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

  it should "be able to encode and decode in no sep case" in {
    import NameCodingStrategies.noSepCase
    implicit val format: Format[AnEvent] = EnumerationFormats[AnEvent]

    val typ: AnEvent = AnEvent.DoneSome
    val json = Json.toJson(typ)

    json.toString() shouldBe "\"donesome\""
    json.as[AnEvent] shouldBe typ
    succeed
  }

}