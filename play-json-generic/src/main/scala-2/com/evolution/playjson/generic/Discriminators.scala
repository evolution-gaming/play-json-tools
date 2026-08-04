package com.evolution.playjson.generic

import shapeless.labelled.FieldType
import shapeless.{:+:, CNil, Coproduct, LabelledGeneric}

import scala.annotation.nowarn
import scala.reflect.ClassTag
import Util.ClassTagOps

/**
  * The names [[NestedTypeWrites]] writes the subtypes of `A` with, so that
  * [[NestedTypeFormat.of]] can report the ones it cannot tell apart. A subtype declared at the top
  * level has no name, since Scala 2 takes the name from lexical nesting and there is nothing above
  * it to take.
  */
trait Discriminators[A] {
  def all: List[Discriminator]
}

object Discriminators {

  def apply[A](implicit discriminators: Discriminators[A]): Discriminators[A] = discriminators

  def create[A](values: List[Discriminator]): Discriminators[A] = new Discriminators[A] {
    def all: List[Discriminator] = values
  }

  implicit def cnilDiscriminators: Discriminators[CNil] = create(Nil)

  implicit def cconsDiscriminators[Key <: Symbol, Head, Tail <: Coproduct](implicit
    tail: Discriminators[Tail],
    tag: ClassTag[Head]
  ): Discriminators[FieldType[Key, Head] :+: Tail] =
    create(Discriminator(tag.runtimeClass.getName, tag.classFullName()) :: tail.all)

  @nowarn("cat=unused")
  implicit def genericDiscriminators[A, Repr <: Coproduct](implicit
    gen: LabelledGeneric.Aux[A, Repr], // used to reach the coproduct the instances below are built from
    repr: Discriminators[Repr]
  ): Discriminators[A] = create(repr.all)
}
