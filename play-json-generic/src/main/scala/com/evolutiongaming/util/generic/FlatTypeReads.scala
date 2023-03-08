package com.evolutiongaming.util.generic

import play.api.libs.json._
import shapeless._
import shapeless.labelled._

trait FlatTypeReads[T] extends Reads[T] {
  override def reads(jsValue: JsValue): JsResult[T]
}

object FlatTypeReads {

  def apply[A](implicit decode: FlatTypeReads[A]): FlatTypeReads[A] = decode

  def create[A](f: JsValue => JsResult[A]): FlatTypeReads[A] = (json: JsValue) => f(json)

  implicit def cnilReads: FlatTypeReads[CNil] = create[CNil] { _ =>
    JsError("could not decode cnil")
  }

  implicit def cconsReadsWithNameCoding[Key <: Symbol, Head, Tail <: Coproduct](implicit
      key: Witness.Aux[Key],
      headReads: Reads[Head],
      tailReads: FlatTypeReads[Tail],
      nameCodingStrategy: NameCodingStrategy): FlatTypeReads[FieldType[Key, Head] :+: Tail] =
    create[FieldType[Key, Head] :+: Tail] { json =>

      for {
        o <- json.validate[JsObject]
        typ <- (o \ "type").validate[String]
        res <- if (typ == nameCodingStrategy(key.value.name)) {
          headReads reads (o - "type") map { z => Inl(field[Key](z)) }
        }
        else {
          tailReads reads o map { Inr(_) }
        }
      } yield res
    }

  implicit def flatTypeReads[A, Repr <: Coproduct](implicit
      gen: LabelledGeneric.Aux[A, Repr],
      reads: FlatTypeReads[Repr]): FlatTypeReads[A] =
    create[A] { json => reads reads json map { gen.from }
  }
}