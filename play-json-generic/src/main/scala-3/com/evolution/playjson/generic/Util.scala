package com.evolution.playjson.generic

import scala.compiletime.*
import play.api.libs.json.*

private[generic] inline def enrichWithType[A](name: String)(transformName: String => String) =
  summonInline[OWrites[A]].transform(jsObject =>
    JsObject(Seq("type" -> JsString(transformName(name)))) ++ jsObject
  )

private[generic] inline def sanitizeName(prefix: String, name: String) =
  if prefix.isBlank() then name else s"$prefix.$name"
