package com.evolution.playjson.generic

import scala.reflect.ClassTag

object Util {

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
