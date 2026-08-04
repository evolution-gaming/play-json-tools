package com.evolution.playjson.generic

import play.api.libs.json._
import shapeless._
import shapeless.labelled.FieldType

import scala.reflect.ClassTag

/**
  * Writes a sealed hierarchy with a `type` field naming the subtype.
  *
  * On Scala 2 the name comes from where the subtype is lexically nested, so an object between the
  * sealed trait and its subtype becomes part of it: `Wrapper.Inner.Leaf` is written as `Inner.Leaf`.
  * The Scala 3 implementation takes the name from the sealed hierarchy instead and writes `Leaf`,
  * because a plain object is not part of that hierarchy. Documents written by one are therefore not
  * readable by the other wherever the two forms of nesting do not coincide, which they do whenever
  * subtypes sit directly under the sealed trait or under sealed sub-traits.
  *
  * A subtype declared at the top level has no enclosing type to name it after, so it is written with
  * an empty `type`. That is readable while it is the only subtype, and [[NestedTypeFormat.of]]
  * reports it once there are two of them, since then neither can be told from the other.
  */
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
  : NestedTypeWrites[FieldType[Key, Head] :+: Tail] = {
    val discriminator = Util.discriminatorOf(tag)

    NestedTypeWrites.create[FieldType[Key, Head] :+: Tail] {
      _.eliminate(
        head => Json.obj("type" -> discriminator) ++ (headWrites writes head),
        tail => tailWrites writes tail
      )
    }
  }

  implicit def nestedTypeWrites[A, Repr <: Coproduct](implicit
    gen: LabelledGeneric.Aux[A, Repr],
    writes: NestedTypeWrites[Repr]
  ): NestedTypeWrites[A] = NestedTypeWrites.create[A] { a => writes writes gen.to(a) }
}