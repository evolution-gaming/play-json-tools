package com.evolution.playjson.generic

import play.api.libs.json.*

import scala.compiletime.*
import scala.deriving.Mirror
import scala.annotation.nowarn

/** Writes a sealed hierarchy with a `type` field naming the subtype.
  *
  * On Scala 3 the name comes from the sealed hierarchy, so every sealed sub-trait contributes to it
  * and nothing else does: `Wrapper.Inner.Leaf`, nested in a plain object, is written as `Leaf`, while
  * a leaf under a top-level sealed sub-trait `Branch` is written as `Branch.Leaf`. The Scala 2
  * implementation takes the name from lexical nesting instead, writing `Inner.Leaf` and `Leaf` for
  * those two. Documents written by one are therefore not readable by the other wherever the two
  * disagree, and they agree only when the sealed trait is declared at the top level and every
  * subtype sits inside its companion object, either directly or inside the companion of a sealed
  * sub-trait declared there.
  */
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
      case _: EmptyTuple     => Nil
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
