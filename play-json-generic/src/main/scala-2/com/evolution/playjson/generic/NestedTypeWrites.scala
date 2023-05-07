package com.evolution.playjson.generic

import play.api.libs.json._
import shapeless._
import shapeless.labelled.FieldType
import Util.ClassTagOps

import scala.reflect.ClassTag

trait NestedTypeWrites[A] extends Writes[A] {
  override def writes(o: A): JsObject
}

object NestedTypeWrites {

  def apply[A](implicit encode: NestedTypeWrites[A]): Writes[A] = new Writes[A] {
    def writes(o: A): JsValue = encode writes o
  }

  def create[A](f: A => JsObject): NestedTypeWrites[A] = new NestedTypeWrites[A] {
    override def writes(o: A): JsObject = f(o)
  }

  implicit def cnilWrites: NestedTypeWrites[CNil] = NestedTypeWrites.create[CNil] { _ =>
    sys.error("Cannot encode CNil")
  }

  implicit def cconsWrites[Key <: Symbol, Head, Tail <: Coproduct](implicit
    headWrites: OWrites[Head],
    tailWrites: NestedTypeWrites[Tail],
    tag: ClassTag[Head])
  : NestedTypeWrites[FieldType[Key, Head] :+: Tail] =
    NestedTypeWrites.create[FieldType[Key, Head] :+: Tail] {
      _.eliminate(
        head => Json.obj("type" -> tag.classFullName()) ++ (headWrites writes head),
        tail => tailWrites writes tail
      )
    }

  implicit def nestedTypeWrites[A, Repr <: Coproduct](implicit
    gen: LabelledGeneric.Aux[A, Repr],
    writes: NestedTypeWrites[Repr]
  ): NestedTypeWrites[A] = NestedTypeWrites.create[A] { a => writes writes gen.to(a) }
}