package com.evolution.playjson.generic

import scala.deriving.Mirror
import scala.compiletime.*
import play.api.libs.json.*
import scala.annotation.nowarn

/**
  * This is a helper class for creating a `Reads` instance for a sealed trait hierarchy.
  * It will look for a `type` field in the JSON object and use its value to determine which subtype to use.
  * The `type` field will be removed from the JSON object before the subtype's `Reads` is called.
  * 
  * The difference between this class and `NestedTypeReads` is that this class uses the simple name of the subtype
  * instead of the full prefixed name. This means that you cannot use the same simple name for multiple subtypes.
  * 
  * Example:
  * 
  * {{{
  * 
  * sealed trait Parent
  * case class Child1(field1: String) extends Parent
  * case class Child2(field2: Int) extends Parent
  *  
  * object Child1:
  *   given Reads[Child1] = Json.reads[Child1]
  * object Child2:
  *   given Reads[Child2] = Json.reads[Child2]
  * 
  * val reads: FlatTypeReads[Parent] = summon[FlatTypeReads[Parent]]
  * 
  * val json: JsValue = Json.parse("""{"type": "Child1", "field1": "value"}""")
  * val result: JsResult[Parent]  = reads.reads(json) // JsSuccess(Child1(value),)
  * }}}
  */
trait FlatTypeReads[T] extends Reads[T]:
  override def reads(jsValue: JsValue): JsResult[T]

object FlatTypeReads:
  def create[A](f: JsValue => JsResult[A]): FlatTypeReads[A] =
    (json: JsValue) => f(json)

  def apply[A](using ev: FlatTypeReads[A]): FlatTypeReads[A] = ev

  /**
    * This is the first method that will be called when the compiler is looking for an instance of `FlatTypeReads`.
    * It will look for a `type` field in the JSON object and use its value to determine which subtype of `A` to use.
    * Then, it will look for an instance of `Reads` for that subtype and use it to read the JSON object.
    */
  inline given deriveFlatTypeReads[A](using
      m: Mirror.SumOf[A],
      nameCodingStrategy: NameCodingStrategy
  ): FlatTypeReads[A] =
    create[A] { json =>
      for {
        obj <- json.validate[JsObject]
        typ <- (obj \ "type").validate[String]
        result <- deriveReads[A](typ) match
          case Some(reads) => reads.reads(obj - "type")
          case None        => JsError("Failed to find decoder")
      } yield result
    }

  /**
    * Recursively search the given tuple of types for one that matches the given type name and has a `Reads` instance.
    *
    * @param typ the type name to search for
    * @param nameCodingStrategy the naming strategy to use when comparing the type name to the names of the types in 
    *        the tuple
    */
  private inline def deriveReadsForSum[A, T <: Tuple](
      typ: String
  )(using nameCodingStrategy: NameCodingStrategy): Option[Reads[A]] =
    inline erasedValue[T] match
      case _: EmptyTuple => None
      case _: (h *: t) =>
        deriveReads[h](typ) match
          case None        => deriveReadsForSum[A, t](typ)
          case Some(value) => Some(value.asInstanceOf[Reads[A]])

  private inline def deriveReads[A](typ: String)(using nameCodingStrategy: NameCodingStrategy): Option[Reads[A]] =
    summonFrom {
      case m: Mirror.ProductOf[A] =>
        // product (case class or case object)
        val name = constValue[m.MirroredLabel]
        if typ == nameCodingStrategy(name)
        then Some(summonInline[Reads[A]])
        else None
      case m: Mirror.SumOf[A] =>
        // sum (trait)
        deriveReadsForSum[A, m.MirroredElemTypes](typ)
      case v: ValueOf[A] =>
        // Singleton type (object without `case` modifier)
        val name = singletonName[A]
        if typ == nameCodingStrategy(name)
        then Some(summonInline[Reads[A]])
        else None
    }
  end deriveReads
end FlatTypeReads
