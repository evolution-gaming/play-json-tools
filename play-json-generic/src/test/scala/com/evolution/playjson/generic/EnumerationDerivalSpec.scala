package com.evolution.playjson.generic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, Json}

class EnumerationDerivalSpec extends AnyFlatSpec with Matchers {

  it should "be able to encode and decode case object enumerations using default low prio implicit" in {
    implicit val format: Format[AnEvent] = Enumeration[AnEvent].format

    val typ: AnEvent = AnEvent.DoneSome
    val js = Json.toJson(typ)
    js.toString() shouldBe "\"DoneSome\""
    js.as[AnEvent] shouldBe typ
  }

  it should "be able to encode and decode in kebab case" in {
    import NameCodingStrategies.kebabCase
    implicit val format: Format[AnEvent] = Enumeration[AnEvent].format

    val typ: AnEvent = AnEvent.DoneSome
    val json = Json.toJson(typ)

    json.toString() shouldBe "\"done-some\""
    json.as[AnEvent] shouldBe typ
    succeed
  }

  it should "be able to derive formats" in {
    implicit val fmt: Format[AnEvent] = Enumeration[AnEvent].format

    val typ: AnEvent = AnEvent.DoneSome
    val js = Json.toJson(typ)
    js.toString() shouldBe "\"DoneSome\""
    js.as[AnEvent] shouldBe typ
  }

  it should "be able to encode and decode in no sep case" in {
    import NameCodingStrategies.noSepCase
    implicit val format: Format[AnEvent] = Enumeration[AnEvent].format

    val typ: AnEvent = AnEvent.DoneSome
    val json = Json.toJson(typ)

    json.toString() shouldBe "\"donesome\""
    json.as[AnEvent] shouldBe typ
    succeed
  }

}