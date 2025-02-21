package com.evolution.playjson.generic

import play.api.libs.json.OFormat

object NestedTypeFormat:
  def apply[A](using reads: NestedTypeReads[A], writes: NestedTypeWrites[A]): OFormat[A] =
    OFormat(reads.reads(_), writes.writes(_))
