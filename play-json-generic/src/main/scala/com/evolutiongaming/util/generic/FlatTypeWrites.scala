package com.evolutiongaming.util.generic
import play.api.libs.json._
import shapeless._
import shapeless.labelled.FieldType

trait FlatTypeWrites[A] extends Writes[A] {
  override def writes(o: A): JsObject
}

object FlatTypeWrites {

  def apply[A](implicit encode: FlatTypeWrites[A]): Writes[A] = (o: A) => encode writes o

  def create[A](f: A => JsObject): FlatTypeWrites[A] = (o: A) => f(o)

  implicit def cnilWrites: FlatTypeWrites[CNil] = create[CNil] { _ =>
    sys.error("Cannot encode CNil")
  }

  @deprecated("Method exists only for backward compatibility", "0.3.12")
  def cconsWrites[Key <: Symbol, Head, Tail <: Coproduct](
      key: Witness.Aux[Key],
      headWrites: OWrites[Head],
      tailWrites: FlatTypeWrites[Tail]): FlatTypeWrites[FieldType[Key, Head] :+: Tail] =
    cconsWritesWithNameCoding(key, headWrites, tailWrites, NameCodingStrategy.default)

  @deprecated("Method exists only for backward compatibility", "0.3.12")
  def cconsWrites[Key <: Symbol, Head, Tail <: Coproduct](
      key: Witness.Aux[Key],
      headWrites: OWrites[Head],
      tailWrites: FlatTypeWrites[Tail],
      nameCodingStrategy: NameCodingStrategy): FlatTypeWrites[FieldType[Key, Head] :+: Tail] =
  cconsWritesWithNameCoding(key ,headWrites, tailWrites, nameCodingStrategy)

  implicit def cconsWritesWithNameCoding[Key <: Symbol, Head, Tail <: Coproduct](implicit
      key: Witness.Aux[Key],
      headWrites: OWrites[Head],
      tailWrites: FlatTypeWrites[Tail],
      nameCodingStrategy: NameCodingStrategy): FlatTypeWrites[FieldType[Key, Head] :+: Tail] =
    create[FieldType[Key, Head] :+: Tail] {
      _.eliminate(
        head => Json.obj("type" -> s"${ nameCodingStrategy(key.value.name) }") ++ (headWrites writes head),
        tail => tailWrites writes tail
      )
    }

  implicit def flatTypeWrites[A, Repr <: Coproduct](implicit
      gen: LabelledGeneric.Aux[A, Repr],
      writes: FlatTypeWrites[Repr]
  ): FlatTypeWrites[A] = create[A] { a => writes writes gen.to(a) }
}