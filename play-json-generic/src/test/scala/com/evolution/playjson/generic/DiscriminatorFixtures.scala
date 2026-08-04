package com.evolution.playjson.generic

/**
  * A subtype nested in a plain object rather than in a sealed sub-trait. Scala 2 derives the
  * discriminator from lexical nesting and Scala 3 from the sealed hierarchy, so this is where the
  * two disagree.
  */
sealed trait Wrapper

object Wrapper {
  object Inner {
    final case class Leaf(value: Int) extends Wrapper
  }
}

/** A subtype declared at the top level, so there is no enclosing type to strip from its name. */
sealed trait Signal

final case class Ping(id: Int) extends Signal

/**
  * An object that overrides `toString`, which the discriminator must not be derived from. The
  * override is written with parentheses because the Scala 3 derivation calls `toString()`, so the
  * ordinary parameterless form does not even compile there today.
  */
sealed trait Command

object Command {
  object Stop extends Command {
    override def toString(): String = "stop-now"
  }
}

/** Two labels that a lossy naming strategy can collapse onto one another. */
sealed trait Colour

object Colour {
  case object RedLight extends Colour
  case object RedDark extends Colour
}
