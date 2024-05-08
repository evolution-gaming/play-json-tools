package com.evolution.playjson.generic

import play.api.libs.json.*

import scala.deriving.Mirror
import scala.compiletime.*

/**
  * This is a helper class for creating a `Writes` instance for a sealed trait hierarchy.
  * It will add a `type` field to the JSON object with the value of the subtype's simple name (without package prefix).
  * 
  * Example:
  * 
  * {{{
  * sealed trait Parent
  * case class Child1(field1: String) extends Parent
  * case class Child2(field2: Int) extends Parent
  * 
  * object Child1:
  *   given OWrites[Child1] = Json.writes[Child1]
  * object Child2:
  *   given OWrites[Child2] = Json.writes[Child2]
  * 
  * val writes: FlatTypeWrites[Parent] = summon[FlatTypeWrites[Parent]]
  * 
  * val json: JsValue = writes.writes(Child1("value")) // {"type": "Child1", "field1": "value"}
  * }}}
  */
trait FlatTypeWrites[A] extends Writes[A]:
  override def writes(o: A): JsObject

object FlatTypeWrites:
  def apply[A](using ev: FlatTypeWrites[A]): FlatTypeWrites[A] = ev

  def create[A](f: A => JsObject): FlatTypeWrites[A] = (o: A) => f(o)

  inline given deriveFlatTypeWrites[A](using
      m: Mirror.SumOf[A],
      nameCodingStrategy: NameCodingStrategy
  ): FlatTypeWrites[A] =
    // Generate writes instances for all subtypes of A and pick 
    // the one that matches the type of the passed value.
    val writes = summonWrites[m.MirroredElemTypes]
    create { value =>
      writes(m.ordinal(value)).asInstanceOf[FlatTypeWrites[A]].writes(value)
    }

  /**
    * Recursively summon `FlatTypeWrites` instances for all types in the given tuple.
    */
  private inline def summonWrites[T <: Tuple](using
      nameCodingStrategy: NameCodingStrategy
  ): List[FlatTypeWrites[?]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (head *: tail) =>
        summonWrite[head].asInstanceOf[FlatTypeWrites[?]] :: summonWrites[tail]

  private inline def summonWrite[A](using
      nameCodingStrategy: NameCodingStrategy
  ): FlatTypeWrites[A] =
    summonFrom {
      case m: Mirror.ProductOf[A] =>
        val name = constValue[m.MirroredLabel]
        val writes = summonEnrichedWrites[A](nameCodingStrategy(name))
        create(value => writes.writes(value))
      case m: Mirror.SumOf[A] =>
        val allWrites = summonWrites[m.MirroredElemTypes]
        create { value =>
          val idx = m.ordinal(value)
          allWrites(idx).asInstanceOf[FlatTypeWrites[A]].writes(value)
        }
      case valueOf: ValueOf[A] =>
        // Singleton type (object without `case` modifier)
        val name = singletonName[A]
        val writes = summonEnrichedWrites[A](nameCodingStrategy(name))
        create(value => writes.writes(value))
    }
end FlatTypeWrites
