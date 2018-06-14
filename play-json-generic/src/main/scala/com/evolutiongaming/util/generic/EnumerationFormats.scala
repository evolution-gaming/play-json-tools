package com.evolutiongaming.util.generic

import play.api.libs.json._

object EnumerationFormats {
  def apply[A](implicit reads: EnumerationReads[A], writes: EnumerationWrites[A]): Format[A] = Format[A](reads, writes)
}
