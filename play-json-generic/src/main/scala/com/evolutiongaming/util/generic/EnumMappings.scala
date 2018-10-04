package com.evolutiongaming.util.generic

import shapeless.{:+:, CNil, Coproduct, LabelledGeneric, Witness}
import shapeless.labelled.FieldType

case class EnumMappings[A](labels: Map[A, String])

object EnumMappings {

  implicit def enumMappings[A, Repr <: Coproduct](implicit
    gen: LabelledGeneric.Aux[A, Repr],
    e: MappingsAux[A, Repr]
  ): EnumMappings[A] = EnumMappings(e.labels)

  case class MappingsAux[A, Repr](labels: Map[A, String])

  implicit def enumMappingsCNil[A]: MappingsAux[A, CNil] = MappingsAux(Map.empty)

  implicit def enumMappingsCCons[A, K <: Symbol, L <: A, R <: Coproduct](implicit
    l: Witness.Aux[L],
    k: Witness.Aux[K],
    r: MappingsAux[A, R]
  ): MappingsAux[A, FieldType[K, L] :+: R] =
    MappingsAux[A, FieldType[K, L] :+: R](r.labels + (l.value -> k.value.name))
}