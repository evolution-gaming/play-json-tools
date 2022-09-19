package play.api.libs.json

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}

/**
  * INTERNAL API: It is an internal implementation for `com.evolutiongaming.jsonitertool.PlayJsonJsoniter`.
  */
object JsonValueCodecJsValue {

  def apply(bigDecimalParseSettings: BigDecimalParseSettings): JsonValueCodec[JsValue] =
    new JsonValueCodec[JsValue] {
      def decodeValue(in: JsonReader, default: JsValue): JsValue = {
        val b = in.nextToken()
        if (b == '"') {
          in.rollbackToken()
          new JsString(in.readString(null))
        } else if (b == 'f' || b == 't') {
          in.rollbackToken()
          if (in.readBoolean()) JsTrue
          else JsFalse
        } else if ((b >= '0' && b <= '9') || b == '-') {
          in.rollbackToken()
          new JsNumber(in.readBigDecimal(
            null,
            bigDecimalParseSettings.mathContext,
            bigDecimalParseSettings.scaleLimit,
            bigDecimalParseSettings.digitsLimit))
        } else if (b == '[') {
          if (in.isNextToken(']')) JsArray.empty
          else {
            in.rollbackToken()
            var vs = new Array[JsValue](8)
            var i = 0
            while ({
              if (i == vs.length) vs = java.util.Arrays.copyOf(vs, i << 1)
              vs(i) = decodeValue(in, default)
              i += 1
              in.isNextToken(',')
            }) ()
            if (in.isCurrentToken(']')) new JsArray({
              if (i == vs.length) vs
              else java.util.Arrays.copyOf(vs, i)
            }) else in.arrayEndOrCommaError()
          }
        } else if (b == '{') {
          if (in.isNextToken('}')) JsObject.empty
          else {
            in.rollbackToken()
            val kvs = new java.util.LinkedHashMap[String, JsValue](8)
            while ({
              kvs.put(in.readKeyAsString(), decodeValue(in, default))
              in.isNextToken(',')
            }) ()
            if (in.isCurrentToken('}')) new JsObject({
              import scala.jdk.CollectionConverters._
              kvs.asScala
            }) else in.objectEndOrCommaError()
          }
        } else in.readNullOrError(default, "expected JSON value")
      }

      def encodeValue(jsValue: JsValue, out: JsonWriter): Unit =
        jsValue match {
          case s: JsString =>
            out.writeVal(s.value)
          case b: JsBoolean =>
            out.writeVal(b.value)
          case n: JsNumber =>
            out.writeVal(n.value)
          case a: JsArray =>
            out.writeArrayStart()
            a.value.foreach(encodeValue(_, out))
            out.writeArrayEnd()
          case o: JsObject =>
            out.writeObjectStart()
            o.underlying.foreach { kv =>
              out.writeKey(kv._1)
              encodeValue(kv._2, out)
            }
            out.writeObjectEnd()
          case _ =>
            out.writeNull()
        }

      val nullValue: JsValue = JsNull
    }
}
