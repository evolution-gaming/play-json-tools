package com.evolution.playjson.generic

import play.api.libs.json._

/** Reads and writes the values of `A` as the labels a [[NameCodingStrategy]] gives their names. */
object EnumerationFormat {

  /**
    * `Left` when the naming strategy gives one label to several values, which would leave all but
    * one of them unreadable and have them all write the same JSON. The labels are only known once
    * the strategy is, which is why this is where they are checked.
    */
  def of[A](implicit
    enumMappings: EnumMappings[A],
    nameCodingStrategy: NameCodingStrategy
  ): Either[String, Format[A]] = {
    val labelsLookup = enumMappings.labels.map { case (value, name) => (value, nameCodingStrategy(name)) }

    val collisions = labelsLookup.groupBy { case (_, label) => label }.collect {
      case (label, values) if values.size > 1 => s"$label from ${ values.keys.mkString(" and ") }"
    }

    if (collisions.isEmpty) Right(formatOf(labelsLookup))
    else Left(s"The naming strategy gives one label to several values: ${ collisions.mkString("; ") }")
  }

  private def formatOf[A](labelsLookup: Map[A, String]): Format[A] = new Format[A] {

    val valuesLookup: Map[String, A] = labelsLookup.map(_.swap)

    def writes(o: A): JsValue = JsString(labelsLookup(o))

    def reads(json: JsValue): JsResult[A] = {
      for {
        label <- json.validate[JsString]
        value <- valuesLookup.get(label.value) match {
          case Some(value) => JsSuccess(value)
          case None        => JsError(s"Cannot parse ${ label.value }")
        }
      } yield value
    }
  }
}
