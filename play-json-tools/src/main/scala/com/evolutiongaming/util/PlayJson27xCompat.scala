package com.evolutiongaming.util

import play.api.libs.json.{JsObject, Json, Writes}

object PlayJson27xCompat {

  implicit def mapWritesPlayJson27xCompat[K, V](implicit
    writesK: Writes[K],
    writesV: Writes[V]
  ): Writes[Map[K, V]] = {
    as: Map[K, V] =>
      val as1 = as.toList
      val as2 = as1.collect { case (k: String, v) => (k, v) }
      if (as1.size == as2.size) {
        val as3 = as2.map { case (k, v) => (k, writesV.writes(v)) }
        JsObject(as3)
      } else {
        Json.toJson(as1)
      }
  }
}
