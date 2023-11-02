package com.evolution.playjson.generic

import play.api.libs.json.*

import scala.compiletime.*
import scala.deriving.Mirror
import scala.annotation.nowarn

trait NestedTypeWrites[A] extends Writes[A]:
  override def writes(o: A): JsObject

object NestedTypeWrites:
  def apply[A](using ev: NestedTypeWrites[A]): NestedTypeWrites[A] = ev

  def create[A](f: A => JsObject): NestedTypeWrites[A] = (value: A) => f(value)

  inline def summonWrite[A](prefix: String): NestedTypeWrites[A] =
    summonFrom {
      case m: Mirror.ProductOf[A] =>
        val name = constValue[m.MirroredLabel]
        val writes = summonEnrichedWrites[A](prefixName(prefix, name))
        create(value => writes.writes(value))
      case m: Mirror.SumOf[A] =>
        val sumName = constValue[m.MirroredLabel]
        val allWrites = summonWrites[m.MirroredElemTypes](prefixName(prefix, sumName))
        create[A] { value =>
          val idx = m.ordinal(value)
          allWrites(idx).asInstanceOf[NestedTypeWrites[A]].writes(value)
        }
      case valueOf: ValueOf[A] =>
        // singleton type (object without `case` modifier)
        val name = singletonName[A]
        val writes = summonEnrichedWrites[A](prefixName(prefix, name))
        create(value => writes.writes(value))
    }

  inline def summonWrites[T <: Tuple](
      prefix: String
  ): List[NestedTypeWrites[?]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (head *: tail) =>
        summonWrite[head](prefix) :: summonWrites[tail](prefix)

  inline given deriveNestedTypeWrites[A](using
      m: Mirror.SumOf[A]
  ): NestedTypeWrites[A] =
    val writes = summonWrites[m.MirroredElemTypes](prefix = "")
    create { value =>
      val idx = m.ordinal(value)
      writes(idx).asInstanceOf[NestedTypeWrites[A]].writes(value)
    }
end NestedTypeWrites
