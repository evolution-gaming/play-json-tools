package com.evolution.playjson.generic

import play.api.libs.json.*

import scala.deriving.Mirror
import scala.compiletime.*

trait FlatTypeWrites[A] extends Writes[A]:
  override def writes(o: A): JsObject

object FlatTypeWrites:
  def apply[A](using ev: FlatTypeWrites[A]): FlatTypeWrites[A] = ev

  def create[A](f: A => JsObject): FlatTypeWrites[A] = (o: A) => f(o)

  inline given deriveFlatTypeWrites[A](using
      m: Mirror.SumOf[A],
      nameCodingStrategy: NameCodingStrategy
  ): FlatTypeWrites[A] =
    val writes = summonWrites[m.MirroredElemTypes]
    create { value =>
      writes(m.ordinal(value)).asInstanceOf[FlatTypeWrites[A]].writes(value)
    }

  inline def summonWrites[T <: Tuple](using
      nameCodingStrategy: NameCodingStrategy
  ): List[FlatTypeWrites[?]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (head *: tail) =>
        summonWrite[head].asInstanceOf[FlatTypeWrites[?]] :: summonWrites[tail]

  inline def summonWrite[A](using
      nameCodingStrategy: NameCodingStrategy
  ): FlatTypeWrites[A] =
    summonFrom {
      case m: Mirror.ProductOf[A] =>
        val name = constValue[m.MirroredLabel]
        val writes = enrichWithType[A](nameCodingStrategy(name))(identity)
        create(value => writes.writes(value))
      case m: Mirror.SumOf[A] =>
        val allWrites = summonWrites[m.MirroredElemTypes]
        create { value =>
          val idx = m.ordinal(value)
          allWrites(idx).asInstanceOf[FlatTypeWrites[A]].writes(value)
        }
      case valueOf: ValueOf[A] =>
        // singleton type (object without `case` modifier)
        val name = valueOf.value.toString().split("\\$").dropRight(1).last
        val writes = enrichWithType[A](nameCodingStrategy(name))(identity)
        create(value => writes.writes(value))
    }
end FlatTypeWrites
