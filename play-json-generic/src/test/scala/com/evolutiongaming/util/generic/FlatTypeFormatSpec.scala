package com.evolutiongaming.util.generic

import play.api.libs.json._
import Message._

class FlatTypeFormatSpec extends JsonFormatSpec {

  "FlatTypeFormat" should {

    implicit val messageFormat = {

      implicit val noopFormat: OFormat[Noop.type] = new OFormat[Noop.type] {
        def writes(o: Noop.type): JsObject = Json.obj()
        def reads(json: JsValue): JsResult[Message.Noop.type] = JsSuccess(Message.Noop)
      }

      implicit val ackFormat: OFormat[Out.Ack.type] = new OFormat[Out.Ack.type] {
        def writes(o: Out.Ack.type): JsObject= Json.obj()
        def reads(json: JsValue): JsResult[Out.Ack.type] = JsSuccess(Out.Ack)
      }

      implicit val updateFormat: OFormat[In.Update] = Json.format[In.Update]
      implicit val updatedFormat: OFormat[Out.Updated] = Json.format[Out.Updated]

      FlatTypeFormat[Message]
    }

    "be able to read and write sealed trait hierarchy using simple name as a type discriminator" in {
      for {
        (o, json) <- Seq(
          Noop -> Json.obj("type" -> "Noop"),
          In.Update(42) -> Json.obj("type" -> "Update", "payload" -> 42),
          Out.Updated("test") -> Json.obj("type" -> "Updated", "v" -> "test"),
          Out.Ack -> Json.obj("type" -> "Ack")
        )
      } yield check[Message](o, json)
    }
  }

}