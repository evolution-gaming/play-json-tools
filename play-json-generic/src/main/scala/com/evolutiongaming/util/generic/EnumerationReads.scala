package com.evolutiongaming.util.generic

import play.api.libs.json._
import shapeless._
import shapeless.labelled._

//adapted from circe
trait EnumerationReads[T] extends Reads[T]{
  override def reads(jsValue: JsValue): JsResult[T]
}

object EnumerationReads {

  def apply[A](implicit decode: EnumerationReads[A]): Reads[A] = decode

  def create[A](f: JsValue => JsResult[A]): EnumerationReads[A] = new EnumerationReads[A] {
    override def reads(jsValue: JsValue): JsResult[A] = f(jsValue)
  }

  def deriveEnumerationReads[A](implicit decode: EnumerationReads[A]): EnumerationReads[A] = decode

  implicit val decodeEnumerationCNil: EnumerationReads[CNil] = create[CNil] { _ =>
    JsError("could not decode cnil")
  }

  implicit def decodeEnumerationCCons[K <: Symbol, V, R <: Coproduct](implicit
    wit: Witness.Aux[K],
    gv: LabelledGeneric.Aux[V, HNil],
    dr: EnumerationReads[R],
    ncs: NameCodingStrategy
  ): EnumerationReads[FieldType[K, V] :+: R] = create[FieldType[K, V] :+: R] { jsValue =>
    jsValue.validate[String] match {
      case JsSuccess(s, _) if  ncs.matches(s, wit.value.name) => JsSuccess(Inl(field[K](gv.from(HNil))))
      case JsSuccess(_, _)                        => dr.reads(jsValue).map(Inr(_))
      case JsError(err)                           => JsError.apply(err)
    }
  }

  implicit def decodeEnumeration[A, Repr <: Coproduct](implicit
    gen: LabelledGeneric.Aux[A, Repr],
    rr: EnumerationReads[Repr]
  ): EnumerationReads[A] = create[A] { jsValue => rr.reads(jsValue).map(gen.from) }
}
