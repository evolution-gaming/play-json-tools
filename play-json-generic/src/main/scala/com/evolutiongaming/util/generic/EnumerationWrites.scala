package com.evolutiongaming.util.generic

import play.api.libs.json.{JsString, JsValue, Writes}
import shapeless._
import shapeless.labelled.FieldType

trait EnumerationWrites[A] extends Writes[A] {
  override def writes(o: A): JsValue
}

//adapted from circe
object EnumerationWrites {
  def apply[A](implicit encode: Lazy[EnumerationWrites[A]]): Writes[A] = encode.value

  def create[A](f: A => JsValue): EnumerationWrites[A] = new EnumerationWrites[A] {
    override def writes(o: A): JsValue = f(o)
  }

  def deriveEnumerationWrites[A](implicit encode: Lazy[EnumerationWrites[A]]): EnumerationWrites[A] = encode.value

  implicit val encodeEnumerationCNil: EnumerationWrites[CNil] = EnumerationWrites.create[CNil] { _ =>
    sys.error("Cannot encode CNil")
  }

  implicit def encodeEnumerationCCons[K <: Symbol, V, R <: Coproduct](implicit
    wit: Witness.Aux[K],
    dr: EnumerationWrites[R],
    ncs: NameCodingStrategy
  ): EnumerationWrites[FieldType[K, V] :+: R] = EnumerationWrites.create[FieldType[K, V] :+: R] {
    case Inl(l) => JsString(ncs.encode(wit.value.name))
    case Inr(r) => dr.writes(r)
  }

  implicit def encodeEnumeration[A, Repr <: Coproduct](implicit
    gen: LabelledGeneric.Aux[A, Repr],
    rr: EnumerationWrites[Repr]
  ): EnumerationWrites[A] = EnumerationWrites.create[A] { a => rr.writes(gen.to(a)) }
}
