package com.evolution.playjson.generic

sealed trait AnEvent
object AnEvent {
  case object DoneSome extends AnEvent
  case object GoneSomeWhere extends AnEvent
}
