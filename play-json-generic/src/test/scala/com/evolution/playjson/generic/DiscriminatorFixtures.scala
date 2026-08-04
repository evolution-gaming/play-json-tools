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
  * Two subtypes declared at the top level. On Scala 2 neither has a name, so they cannot be told
  * apart, which one subtype on its own can be.
  */
sealed trait Relay

final case class Open(id: Int) extends Relay

final case class Close(id: Int) extends Relay

/**
  * A leaf under a sealed sub-trait that is itself declared at the top level. Scala 2 sees one level
  * of nesting to strip and Scala 3 sees a sub-trait to prefix with, so the two disagree.
  */
sealed trait Root

sealed trait Branch extends Root

object Branch {
  final case class Leaf(value: Int) extends Branch
}

/**
  * Two subtypes of the same simple name in different objects, which is all it takes for
  * [[FlatTypeFormat]] to give them one name, since it names by the subtype alone.
  */
sealed trait Duct

object Duct {
  object In {
    final case class Update(a: Int) extends Duct
  }

  object Out {
    final case class Update(b: Int) extends Duct
  }
}

/**
  * Two subtypes of the same name in different plain objects. Scala 2 keeps them apart through
  * lexical nesting, Scala 3 gives both the same name.
  */
sealed trait Tree

object Tree {
  object First {
    final case class Node(value: Int) extends Tree
  }

  object Second {
    final case class Node(value: Int) extends Tree
  }
}

/**
  * A plain object subtype declared at package level, where the two versions part ways again: Scala 3
  * names it with the package still attached, Scala 2 has no name for it at all.
  */
sealed trait Beacon

object Pulse extends Beacon

/** A package-level object that also overrides `toString`, where the two shapes above meet. */
sealed trait Alarm

object Chime extends Alarm {
  override def toString: String = "ring-ring"
}

/** An object that overrides `toString`, which the discriminator must not be derived from. */
sealed trait Command

object Command {
  object Stop extends Command {
    override def toString: String = "stop-now"
  }
}

/**
  * An enumeration whose value overrides `toString`. Scala 2 labels it by its name, Scala 3 by its
  * `toString`.
  */
sealed trait Mood

object Mood {
  case object Cheerful extends Mood {
    override def toString: String = "in-a-good-mood"
  }

  case object Grumpy extends Mood
}

/** Two labels that a lossy naming strategy can collapse onto one another. */
sealed trait Colour

object Colour {
  case object RedLight extends Colour
  case object RedDark extends Colour
}
