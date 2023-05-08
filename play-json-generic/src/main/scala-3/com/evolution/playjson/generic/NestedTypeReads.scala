package com.evolution.playjson.generic

import play.api.libs.json.*
import scala.deriving.Mirror
import scala.compiletime.*

trait NestedTypeReads[T] extends Reads[T]:
  override def reads(jsValue: JsValue): JsResult[T]

object NestedTypeReads:
  def apply[A](using ev: NestedTypeReads[A]): NestedTypeReads[A] = ev

  def create[A](f: JsValue => JsResult[A]): NestedTypeReads[A] =
    (jsValue: JsValue) => f(jsValue)

  inline given derive[A](using m: Mirror.SumOf[A]): NestedTypeReads[A] =
    create[A] { jsValue =>
      for {
        obj <- jsValue.validate[JsObject]
        typ <- (obj \ "type").validate[String]
        result <- deriveNestedTypeReadsForSum[A, m.MirroredElemTypes](typ, prefix = "") match
          case Some(reads) => reads.reads(obj - "type")
          case None        => JsError(s"Could not find a Reads for type $typ")
      } yield result
    }

  private inline def deriveNestedTypeReadsForSum[A, T <: Tuple](
      typ: String,
      prefix: String
  ): Option[Reads[A]] =
    inline erasedValue[T] match
      case _: EmptyTuple => None
      case _: (head *: tail) =>
        deriveNestedTypeReads[head](typ, prefix) match
          case None        => deriveNestedTypeReadsForSum[A, tail](typ, prefix)
          case Some(reads) => Some(reads.asInstanceOf[Reads[A]])

  private inline def deriveNestedTypeReads[A](
      typ: String,
      prefix: String
  ): Option[Reads[A]] =
    summonFrom {
      case m: Mirror.ProductOf[A] =>
        val name = constValue[m.MirroredLabel]
        if (prefixName(prefix, name) == typ) Some(summonInline[Reads[A]])
        else None
      case m: Mirror.SumOf[A] =>
        val sumName = constValue[m.MirroredLabel]
        deriveNestedTypeReadsForSum[A, m.MirroredElemTypes](typ, prefixName(prefix, sumName))
      case valueOf: ValueOf[A] =>
        // Singleton type (object without `case` modifier)
        val name = singletonName[A]
        if (prefixName(prefix, name) == typ) Some(summonInline[Reads[A]])
        else None
    }
end NestedTypeReads
