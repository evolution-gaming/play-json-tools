package com.evolution.playjson.tools

import play.api.libs.json.{Json, Writes}


object ToJsonStr {
  def apply[T](x: T)(implicit writes: Writes[T]): String = {
    val json = writes writes x
    Json stringify json
  }
}