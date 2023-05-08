package com.evolution.playjson.generic

import scala.deriving.Mirror
import scala.compiletime.summonAll

case class EnumMappings[A](labels: Map[A, String])

object EnumMappings:
  inline given valueMap[E](using m: Mirror.SumOf[E]): EnumMappings[E] =
    val singletons = summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]]
    val elems = singletons.toList.asInstanceOf[List[ValueOf[E]]]
    println(EnumMappings(elems.view.map(_.value).map(e => e -> e.toString).toMap))
    EnumMappings(elems.view.map(_.value).map(e => e -> e.toString).toMap)
