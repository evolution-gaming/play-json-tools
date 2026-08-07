package com.evolution.playjson.generic

import scala.reflect.ClassTag

object Util {

  /** The `type` field naming a subtype. Empty for a subtype declared at the top level, whose name
    * consists of the class alone and so has nothing left once the enclosing type is dropped.
    * [[NestedTypeFormat.of]] is where that is reported.
    */
  def discriminatorOf[A](tag: ClassTag[A]): String = new ClassTagOps(tag).classFullName()

  implicit class ClassTagOps[T](val self: ClassTag[T]) extends AnyVal {

    def classFullName(omitBaseClass: Boolean = true): String = {
      val name = self.runtimeClass.getName
      val idx = name.lastIndexOf('.')
      val parts = name.substring(idx + 1).split('$').filterNot(_.isEmpty)

      if (omitBaseClass)
        parts.tail.mkString(".")
      else
        parts.mkString(".")
    }
  }
}
