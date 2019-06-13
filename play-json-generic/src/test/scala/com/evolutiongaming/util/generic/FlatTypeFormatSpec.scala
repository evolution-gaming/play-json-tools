package com.evolutiongaming.util.generic

import play.api.libs.json._
import Message._

class FlatTypeFormatSpec extends JsonFormatSpec {

  "FlatTypeFormat" should {

    def messageFormat(implicit nameCodingStrategy: NameCodingStrategy): Format[Message] = {

      implicit val noopFormat: OFormat[Noop.type] = new OFormat[Noop.type] {
        def writes(o: Noop.type): JsObject = JsObject.empty
        def reads(json: JsValue): JsResult[Message.Noop.type] = JsSuccess(Message.Noop)
      }

      implicit val ackFormat: OFormat[Out.Ack.type] = new OFormat[Out.Ack.type] {
        def writes(o: Out.Ack.type): JsObject = JsObject.empty
        def reads(json: JsValue): JsResult[Out.Ack.type] = JsSuccess(Out.Ack)
      }

      implicit val updateFormat: OFormat[In.Update] = Json.format[In.Update]
      implicit val updatedFormat: OFormat[Out.Updated] = Json.format[Out.Updated]

      FlatTypeFormat[Message]
    }

    "be able to read and write sealed trait hierarchy using simple name as a type discriminator" in {
      implicit val cameCaseTypeFormat: Format[Message] = messageFormat

      for {
        (o, json) <- Seq(
          Noop -> Json.obj("type" -> "Noop"),
          In.Update(42) -> Json.obj("type" -> "Update", "payload" -> 42),
          Out.Updated("test") -> Json.obj("type" -> "Updated", "v" -> "test"),
          Out.Ack -> Json.obj("type" -> "Ack")
        )
      } yield check[Message](o, json)
    }

    "be able to read and write sealed trait hierarchy using lower case name as a type discriminator" in {
      implicit val cameCaseTypeFormat: Format[Message] = messageFormat(NameCodingStrategies.noSepCase)

      for {
        (o, json) <- Seq(
          Noop -> Json.obj("type" -> "noop"),
          In.Update(42) -> Json.obj("type" -> "update", "payload" -> 42),
          Out.Updated("test") -> Json.obj("type" -> "updated", "v" -> "test"),
          Out.Ack -> Json.obj("type" -> "ack")
        )
      } yield check[Message](o, json)
    }
  }

}