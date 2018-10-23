package com.evolutiongaming.util.generic

sealed trait Message

object Message {
  sealed trait In extends Message

  object In {
    final case class Update(payload: Int) extends In
  }

  sealed trait Out extends Message

  object Out {
    object Ack extends Out
    final case class Updated(v: String) extends Out
  }

  object Noop extends Message
}