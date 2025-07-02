package com.evolution.playjson.generic

import play.api.libs.json._

object FlatTypeFormat {
  def apply[A](implicit reads: FlatTypeReads[A], writes: FlatTypeWrites[A]): OFormat[A] =
    OFormat.apply(reads.reads, writes.writes)
}