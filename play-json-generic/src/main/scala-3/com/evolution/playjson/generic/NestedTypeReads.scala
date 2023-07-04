package com.evolution.playjson.generic

import play.api.libs.json.*
import scala.deriving.Mirror
import scala.compiletime.*

/**
  * This is a helper class for creating a `Reads` instance for a sealed trait hierarchy.
  * It will look for a `type` field in the JSON object and use its value to determine which subtype to use.
  * The `type` field will be removed from the JSON object before the subtype's `Reads` is called.
  * 
  * The difference between this class and `FlatTypeReads` is that this class uses the full prefixed name of the subtype
  * instead of just the simple name. This allows you to use the same simple name for multiple subtypes as long as they
  * are located in different branches of the sealed trait hierarchy.
  * 
  * Example:
  * {{{
  * sealed trait Parent
  * sealed trait Branch1 extends Parent
  * sealed trait Branch2 extends Parent
  * case class Child1(field1: String) extends Parent
  * case class Child2(field2: Int) extends Parent
  * 
  * object Child1:
  *   given Reads[Child1] = Json.reads[Child1]
  * object Child2:
  *   given Reads[Child2] = Json.reads[Child2]
  * 
  * val reads: NestedTypeReads[Parent] = summon[NestedTypeReads[Parent]]
  * 
  * }}} 
  * 
  */
trait NestedTypeReads[T] extends Reads[T]:
  override def reads(jsValue: JsValue): JsResult[T]

object NestedTypeReads:
  def apply[A](using ev: NestedTypeReads[A]): NestedTypeReads[A] = ev

  def create[A](f: JsValue => JsResult[A]): NestedTypeReads[A] =
    (jsValue: JsValue) => f(jsValue)

  /**
    * This is the first method that will be called when the compiler is looking for an instance of `NestedTypeReads`.
    * It will look for a `type` field in the JSON object and use its value to determine which subtype of `A` to use.
    * Then, it will look for an instance of `Reads` for that subtype and use it to read the JSON object.
    *
    * @param m
    * @return
    */
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

  /**
    * Traverse the tuple of types and look for a `Reads` instance for the type that matches the given `typ`.
    * If no `Reads` instance is found, return `None`. Otherwise, return the `Reads` instance.
    * This method is used to traverse the tuple of types that represent the subtypes of a sealed trait.
    *
    * @param typ the value of the `type` field in the JSON object.
    * @param prefix the prefix to use when building the full prefixed name of the subtype. 
    *        For example, if the `prefix` is `com.example` and the `typ` is `Child1`, the full prefixed name of the subtype
    *        is `com.example.Child1`. If the `prefix` is empty, the full prefixed name of the subtype is just the `typ`.
    */
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
