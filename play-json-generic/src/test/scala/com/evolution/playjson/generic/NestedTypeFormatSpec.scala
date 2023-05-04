package com.evolution.playjson.generic

import play.api.libs.json._
import Message._

class NestedTypeFormatSpec extends JsonFormatSpec {

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

    NestedTypeFormat[Message]
  }

  "NestedTypeFormat" should {

    "be able to read and write sealed trait hierarchy using full type name as a discriminator" in {
      for {
        (o, json) <- Seq(
          Noop -> Json.obj("type" -> "Noop"),
          In.Update(42) -> Json.obj("type" -> "In.Update", "payload" -> 42),
          Out.Updated("test") -> Json.obj("type" -> "Out.Updated", "v" -> "test"),
          Out.Ack -> Json.obj("type" -> "Out.Ack")
        )
      } yield check[Message](o, json)
    }
  }
}