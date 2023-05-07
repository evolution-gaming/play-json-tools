package com.evolution.playjson.generic

import scala.deriving.Mirror
import scala.compiletime.*
import play.api.libs.json.*
import scala.annotation.nowarn

trait FlatTypeReads[T] extends Reads[T]:
  override def reads(jsValue: JsValue): JsResult[T]

object FlatTypeReads:
  def create[A](f: JsValue => JsResult[A]): FlatTypeReads[A] =
    (json: JsValue) => f(json)

  def apply[A](using ev: FlatTypeReads[A]): FlatTypeReads[A] = ev

  inline given deriveFlatTypeReads[A](using
      m: Mirror.SumOf[A],
      nameCodingStrategy: NameCodingStrategy
  ): FlatTypeReads[A] =
    create[A] { json =>
      for {
        obj <- json.validate[JsObject]
        typ <- (obj \ "type").validate[String]
        result <- deriveReads[A]((obj), typ) match
          case Some(reads) => reads.reads(obj - "type")
          case None        => JsError("Failed to find decoder")
      } yield result
    }

  inline def deriveReadsForSum[A, T <: Tuple](
      json: JsObject,
      typ: String
  )(using nameCodingStrategy: NameCodingStrategy): Option[Reads[A]] = {
    inline erasedValue[T] match
      case _: EmptyTuple => None
      case _: (h *: t) =>
        deriveReads[h](json, typ) match
          case None        => deriveReadsForSum[A, t](json, typ)
          case Some(value) => Some(value.asInstanceOf[Reads[A]])
  }

  inline def deriveReads[A](json: JsObject, typ: String)(using nameCodingStrategy: NameCodingStrategy): Option[Reads[A]] =
    summonFrom {
      case m: Mirror.ProductOf[A] =>
        // product (case class or case object)
        val name = constValue[m.MirroredLabel].asInstanceOf[String]
        if typ == nameCodingStrategy(name)
        then
          val reads = summonInline[Reads[A]]
          Some(Reads(jsValue => reads.reads(json).asInstanceOf[JsResult[A]]))
        else None
      case m: Mirror.SumOf[A] =>
        // sum (trait)
        deriveReadsForSum[A, m.MirroredElemTypes](json, typ)
      case v: ValueOf[A] =>
        // singleton type (object without `case` modifier)
        val name = v.value.toString().split("\\$").dropRight(1).last
        if typ == nameCodingStrategy(name)
        then
          val reads = summonInline[Reads[A]]
          Some(Reads(jsValue => reads.reads(json).asInstanceOf[JsResult[A]]))
        else None
    }
  end deriveReads
end FlatTypeReads
