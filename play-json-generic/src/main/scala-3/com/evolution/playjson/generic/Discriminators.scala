package com.evolution.playjson.generic

import scala.compiletime.*
import scala.deriving.Mirror

/**
  * The names [[NestedTypeWrites]] writes the subtypes of `A` with, so that
  * [[NestedTypeFormat.of]] can report the ones it cannot tell apart. Every subtype has a name here,
  * since Scala 3 takes it from the sealed hierarchy, but two subtypes can share one: a plain object
  * around them contributes nothing, so subtypes of the same name in different objects collide.
  */
trait Discriminators[A]:
  def all: List[Discriminator]

object Discriminators:

  inline def apply[A](using discriminators: Discriminators[A]): Discriminators[A] = discriminators

  def create[A](values: List[Discriminator]): Discriminators[A] = new Discriminators[A]:
    def all: List[Discriminator] = values

  inline def discriminatorsOf[A](prefix: String): List[Discriminator] =
    summonFrom {
      case m: Mirror.ProductOf[A] =>
        val label = constValue[m.MirroredLabel]
        List(Discriminator(label, prefixName(prefix, label)))
      case m: Mirror.SumOf[A] =>
        val sumName = constValue[m.MirroredLabel]
        discriminatorsOfAll[m.MirroredElemTypes](prefixName(prefix, sumName))
      case valueOf: ValueOf[A] =>
        // singleton type (object without `case` modifier)
        val name = singletonName[A]
        List(Discriminator(name, prefixName(prefix, name)))
    }

  inline def discriminatorsOfAll[T <: Tuple](prefix: String): List[Discriminator] =
    inline erasedValue[T] match
      case _: EmptyTuple     => Nil
      case _: (head *: tail) => discriminatorsOf[head](prefix) ++ discriminatorsOfAll[tail](prefix)

  inline given deriveDiscriminators[A](using m: Mirror.SumOf[A]): Discriminators[A] =
    create(discriminatorsOfAll[m.MirroredElemTypes](prefix = ""))
end Discriminators
