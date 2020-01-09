package com.evolutiongaming.jsonitertool

import com.github.plokhotnyuk.jsoniter_scala.core._
import play.api.libs.json._

import scala.collection.IndexedSeq

object PlayJsonJsoniter {

  implicit val jsValueCodec: JsonValueCodec[JsValue] =
    new JsonValueCodec[JsValue] {

      /**
       * The implementation was borrowed from: https://github.com/plokhotnyuk/jsoniter-scala/blob/e80d51019b39efacff9e695de97dce0c23ae9135/jsoniter-scala-benchmark/src/main/scala/io/circe/CirceJsoniter.scala
       * and adapted to meet PlayJson criteria.
       */
      override def decodeValue(in: JsonReader, default: JsValue): JsValue = {
        val b = in.nextToken()
        if (b == 'n') in.readNullOrError(default, "expected `null` value")
        else if (b == '"') {
          in.rollbackToken()
          JsString(in.readString(null))
        } else if (b == 'f' || b == 't') {
          in.rollbackToken()
          if (in.readBoolean()) JsTrue else JsFalse
        } else if ((b >= '0' && b <= '9') || b == '-') {
          in.rollbackToken()
          val s = JsonParserSettings.settings.bigDecimalParseSettings
          //In order to stay consistent with PlayJson which can parse 310 characters length numbers
          val dLimit = s.digitsLimit + 1
          JsNumber(in.readBigDecimal(null, s.mathContext, s.scaleLimit, dLimit))
        } else if (b == '[') {
          val array: IndexedSeq[JsValue] =
            if (in.isNextToken(']')) new Array[JsValue](0)
            else {
              in.rollbackToken()
              var i = 0
              var arr = new Array[JsValue](4)
              do {
                if (i == arr.length) arr = java.util.Arrays.copyOf(arr, i << 1)
                arr(i) = decodeValue(in, default)
                i += 1
              } while (in.isNextToken(','))

              if (in.isCurrentToken(']'))
                if (i == arr.length) arr else java.util.Arrays.copyOf(arr, i)
              else in.arrayEndOrCommaError()
            }
          JsArray(array)
        } else if (b == '{') {
           /*
            * Because of DoS vulnerability in Scala 2.12 HashMap https://github.com/scala/bug/issues/11203
            * we use a Java LinkedHashMap because it better handles hash code collisions for Comparable keys.
            */
          val kvs =
            if (in.isNextToken('}')) new java.util.LinkedHashMap[String, JsValue]()
            else {
              val underlying = new java.util.LinkedHashMap[String, JsValue]()
              in.rollbackToken()
              do {
                underlying.put(in.readKeyAsString(), decodeValue(in, default))
              } while (in.isNextToken(','))

              if (!in.isCurrentToken('}'))
                in.objectEndOrCommaError()

              underlying
            }
          import scala.jdk.CollectionConverters._
          JsObject(kvs.asScala)
        } else in.decodeError("expected JSON value")
      }

      override def encodeValue(jsValue: JsValue, out: JsonWriter): Unit =
        jsValue match {
          case JsBoolean(b) =>
            out.writeVal(b)
          case JsString(value) =>
            out.writeVal(value)
          case JsNumber(value) =>
            out.writeVal(value)
          case JsArray(items) =>
            out.writeArrayStart()
            items.foreach(encodeValue(_, out))
            out.writeArrayEnd()
          case JsObject(kvs) =>
            out.writeObjectStart()
            kvs.foreach {
              case (k, v) =>
                out.writeKey(k)
                encodeValue(v, out)
            }
            out.writeObjectEnd()
          case JsNull =>
            out.writeNull()
        }

      override val nullValue: JsValue = JsNull
    }

  def serialize(payload: JsValue): Array[Byte] =
    writeToArray(payload)

  def deserialize(bytes: Array[Byte]): JsValue =
    readFromArray[JsValue](bytes)
}
