package com.evolutiongaming.util.generic

import play.api.libs.json._
import shapeless._
import shapeless.labelled._
import Util.ClassTagOps

import scala.reflect.ClassTag

trait NestedTypeReads[T] extends Reads[T] {
  override def reads(jsValue: JsValue): JsResult[T]
}

object NestedTypeReads {

  def apply[A](implicit decode: NestedTypeReads[A]): NestedTypeReads[A] = decode

  def create[A](f: JsValue => JsResult[A]): NestedTypeReads[A] = new NestedTypeReads[A] {
    override def reads(json: JsValue): JsResult[A] = f(json)
  }

  implicit def cnilReads: NestedTypeReads[CNil] = create[CNil] { _ =>
    JsError("could not decode cnil")
  }

  implicit def cconsReads[Key <: Symbol, Head, Tail <: Coproduct](implicit
      headReads: Reads[Head],
      tailReads: NestedTypeReads[Tail],
      tag: ClassTag[Head]): NestedTypeReads[FieldType[Key, Head] :+: Tail] =
    create[FieldType[Key, Head] :+: Tail] { json =>

      for {
        o <- json.validate[JsObject]
        typ <- (o \ "type").validate[String]
        res <- if (typ == tag.classFullName()) {
          headReads reads (o - "type") map { z => Inl(field[Key](z)) }
        }
        else {
          tailReads reads o map { Inr(_) }
        }
      } yield res
    }

  implicit def nestedTypeReads[A, Repr <: Coproduct](implicit
      gen: LabelledGeneric.Aux[A, Repr],
      reads: NestedTypeReads[Repr]): NestedTypeReads[A] =
    create[A] {
      json => reads reads json map { gen.from }
    }
}