package com.evolution.playjson.generic

import scala.deriving.Mirror
import scala.compiletime.summonAll

case class EnumMappings[A](labels: Map[A, String])

object EnumMappings:
  inline given valueMap[E](using m: Mirror.SumOf[E]): EnumMappings[E] =
    // First, we make a compile-time check that all of subtypes of E are singletons
    // (i.e. case objects) by requiring that there's an instance of ValueOf for each subtype.
    val singletons = summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]]
    // Then, we can safely obtain a list of ValueOf instances and map each subtype to its string representation.
    val elems = singletons.toList.asInstanceOf[List[ValueOf[E]]]
    EnumMappings(elems.view.map(_.value).map(e => e -> e.toString).toMap)
