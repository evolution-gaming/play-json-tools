package com.evolution.playjson.generic

import play.api.libs.json._

object FlatTypeFormat:
  def apply[A](using reads: FlatTypeReads[A], writes: FlatTypeWrites[A]): OFormat[A] =
    OFormat(reads reads _, writes writes _)