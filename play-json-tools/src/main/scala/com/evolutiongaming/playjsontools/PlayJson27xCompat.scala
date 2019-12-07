package com.evolutiongaming.playjsontools

import play.api.libs.json.{JsArray, JsValue, OWrites, Writes}

import scala.collection.immutable.Iterable
import scala.collection.mutable

object PlayJson27xCompat {

  implicit def iterableWrites[A, B](implicit ev: B <:< Iterable[A], writes: Writes[A]): Writes[B] = {
    Writes[B] { as =>
      val builder = mutable.ArrayBuilder.make[JsValue]
      as.foreach { a: A => builder += writes.writes(a) }
      JsArray(builder.result())
    }
  }

  implicit def mapWrites[A: Writes]: OWrites[Map[String, A]] = Writes.mapWrites[A]
}
