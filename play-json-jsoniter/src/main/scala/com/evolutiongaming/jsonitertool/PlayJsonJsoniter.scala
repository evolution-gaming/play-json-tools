package com.evolutiongaming.jsonitertool

import com.github.plokhotnyuk.jsoniter_scala.core._
import play.api.libs.json._

object PlayJsonJsoniter {

  implicit val jsValueCodec: JsonValueCodec[JsValue] =
    new JsonValueCodec[JsValue] {

      /**
       * The implementation was borrowed from: https://github.com/plokhotnyuk/jsoniter-scala/blob/e80d51019b39efacff9e695de97dce0c23ae9135/jsoniter-scala-benchmark/src/main/scala/io/circe/CirceJsoniter.scala
       * and adapted to meet PlayJson criteria.
       */
      override def decodeValue(in: JsonReader, default: JsValue): JsValue = {
        var b = in.nextToken
        if (b == 'n') in.readNullOrError(default, "expected `null` value")
        else if (b == '"') {
          in.rollbackToken()
          JsString(in.readString(null))
        } else if (b == 'f' || b == 't') {
          in.rollbackToken()
          if (in.readBoolean()) JsTrue else JsFalse
        } else if (b == '-' || (b >= '0' && b <= '9')) {
          JsNumber(
            {
              in.rollbackToken
              in.setMark
              try {
                do {
                  b = in.nextByte
                } while ((b >= '0' && b <= '9') || b == '-')
              } catch {
                case _: JsonReaderException => /* ignore end of input error */
              } finally in.rollbackToMark
              //PlayJson specific thing, since it uses BigDecimal to represent all numbers.
              in.readBigDecimal(null)
            }
          )
        } else if (b == '[') {
          JsArray(
            if (in.isNextToken(']')) Vector.empty[JsValue]
            else {
              in.rollbackToken()
              var i = 0
              var arr = new Array[JsValue](4)
              do {
                if (i == arr.length) arr = java.util.Arrays.copyOf(arr, i << 1)
                arr(i) = decodeValue(in, default)
                i += 1
              } while (in.isNextToken(','))

              (if (in.isCurrentToken(']'))
                if (i == arr.length) arr else java.util.Arrays.copyOf(arr, i)
              else in.arrayEndOrCommaError()).toVector
            })
        } else if (b == '{') {
          new JsObject(
            if (in.isNextToken('}')) scala.collection.mutable.Map.empty[String, JsValue]
            else {
              val underlying = scala.collection.mutable.LinkedHashMap[String, JsValue]()
              in.rollbackToken()

              do {
                underlying.put(in.readKeyAsString(), decodeValue(in, default))
              } while (in.isNextToken(','))

              if (!in.isCurrentToken('}'))
                in.objectEndOrCommaError()

              underlying
            }
          )
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

  /**
   * @see See [[com.github.plokhotnyuk.jsoniter_scala.core.writeToArray]]
   */
  def serialize(payload: JsValue): Array[Byte] =
    writeToArray(payload)

  /**
   * @see See [[com.github.plokhotnyuk.jsoniter_scala.core.readFromArray]]
   */
  def deserialize(bytes: Array[Byte]): JsValue =
    readFromArray[JsValue](bytes)
}
