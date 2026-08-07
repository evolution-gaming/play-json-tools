package play.api.libs.json

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}

import java.math.{BigDecimal => JavaBigDecimal}
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec

/** INTERNAL API: It is an internal implementation for [[com.evolution.playjson.jsoniter.PlayJsonJsoniter]]`.
  *
  * Numbers are written as play-json's own JVM serializer writes them: trailing zeros stripped, and
  * scientific notation outside `[minPlain, maxPlain]` of the `BigDecimalSerializerConfig`. A number
  * that `decodeValue` could not read back under the `BigDecimalParseConfig` limits is refused with
  * `encodeError` rather than written, so the codec never produces a document it cannot parse. The
  * refusal happens where the number sits in the document, so a writer that streams its buffer may
  * already have emitted the part before it.
  */
object JsonValueCodecJsValue {

  // A value of at most this many significant digits, written plainly, spans magnitudes from
  // 1e-6 up to (but not including) 1e18, and takes at most 24 digits to write.
  private val FastPathMostSignificantDigits = 18
  private val FastPathMostDigits = 24
  private val FastPathSmallestValue = BigDecimal("1e-6")
  private val FastPathLargestValue = BigDecimal("1e18")

  /** Jackson's default, and therefore the depth play-json reads. Matching it keeps both backends
    * accepting the same documents, and keeps this codec from writing one play-json cannot read.
    */
  val DefaultMaxNestingDepth: Int = 1000

  @deprecated(
    "Reading settings are given here, writing settings are taken from the global " +
      "JsonConfig.settings. Pass both explicitly instead.",
    "1.4.0"
  )
  def apply(bigDecimalParseSettings: BigDecimalParseConfig): JsonValueCodec[JsValue] =
    apply(bigDecimalParseSettings, JsonConfig.settings.bigDecimalSerializerConfig)

  def apply(
      bigDecimalParseSettings: BigDecimalParseConfig,
      bigDecimalSerializerSettings: BigDecimalSerializerConfig
  ): JsonValueCodec[JsValue] =
    apply(bigDecimalParseSettings, bigDecimalSerializerSettings, DefaultMaxNestingDepth)

  def apply(
      bigDecimalParseSettings: BigDecimalParseConfig,
      bigDecimalSerializerSettings: BigDecimalSerializerConfig,
      maxNestingDepth: Int
  ): JsonValueCodec[JsValue] =
    new JsonValueCodec[JsValue] {
      def decodeValue(in: JsonReader, default: JsValue): JsValue = decodeValue(in, default, 0)

      private def decodeValue(in: JsonReader, default: JsValue, depth: Int): JsValue = {
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
            bigDecimalParseSettings.digitsLimit
          ))
        } else if (b == '[') {
          val level = nextLevel(in, depth)
          if (in.isNextToken(']')) JsArray.empty
          else {
            in.rollbackToken()
            var vs = new Array[JsValue](8)
            var i = 0
            while ({
              if (i == vs.length) vs = java.util.Arrays.copyOf(vs, i << 1)
              vs(i) = decodeValue(in, default, level)
              i += 1
              in.isNextToken(',')
            }) ()
            if (in.isCurrentToken(']')) new JsArray({
              if (i == vs.length) vs
              else java.util.Arrays.copyOf(vs, i)
            })
            else in.arrayEndOrCommaError()
          }
        } else if (b == '{') {
          val level = nextLevel(in, depth)
          if (in.isNextToken('}')) JsObject.empty
          else {
            in.rollbackToken()
            val kvs = new java.util.LinkedHashMap[String, JsValue](8)
            while ({
              kvs.put(in.readKeyAsString(), decodeValue(in, default, level))
              in.isNextToken(',')
            }) ()
            if (in.isCurrentToken('}')) new JsObject({
              import scala.jdk.CollectionConverters._
              kvs.asScala
            })
            else in.objectEndOrCommaError()
          }
        } else in.readNullOrError(default, "expected JSON value")
      }

      def encodeValue(jsValue: JsValue, out: JsonWriter): Unit = encodeValue(jsValue, out, 0)

      private def encodeValue(jsValue: JsValue, out: JsonWriter, depth: Int): Unit =
        jsValue match {
          case s: JsString =>
            out.writeVal(s.value)
          case b: JsBoolean =>
            out.writeVal(b.value)
          case n: JsNumber =>
            encodeBigDecimal(n.value, out)
          case a: JsArray =>
            val level = nextLevel(out, depth)
            out.writeArrayStart()
            a.value.foreach(encodeValue(_, out, level))
            out.writeArrayEnd()
          case o: JsObject =>
            val level = nextLevel(out, depth)
            out.writeObjectStart()
            o.underlying.foreach { kv =>
              out.writeKey(kv._1)
              encodeValue(kv._2, out, level)
            }
            out.writeObjectEnd()
          case _ =>
            out.writeNull()
        }

      private def nextLevel(in: JsonReader, depth: Int): Int = {
        val level = depth + 1
        if (level > maxNestingDepth) in.decodeError(s"depth of nesting exceeds $maxNestingDepth")
        level
      }

      private def nextLevel(out: JsonWriter, depth: Int): Int = {
        val level = depth + 1
        if (level > maxNestingDepth) out.encodeError(s"depth of nesting exceeds $maxNestingDepth")
        level
      }

      /** Whether the configured settings are wide enough for [[needsNoNormalization]] to imply
        * play-json parity. Computed once: the reasoning there is about magnitudes and digit counts,
        * and only holds while the plain range and the parse limits contain them.
        */
      private val fastPathAgreesWithPlayJson: Boolean =
        bigDecimalSerializerSettings.minPlain < FastPathSmallestValue &&
          bigDecimalSerializerSettings.maxPlain >= FastPathLargestValue &&
          bigDecimalParseSettings.digitsLimit > FastPathMostDigits &&
          bigDecimalParseSettings.scaleLimit > FastPathMostDigits

      private def encodeBigDecimal(value: BigDecimal, out: JsonWriter): Unit = {
        val decimal = value.bigDecimal
        if (fastPathAgreesWithPlayJson && isSmallWholeNumber(decimal)) out.writeVal(decimal.longValueExact)
        else if (fastPathAgreesWithPlayJson && needsNoNormalization(decimal)) out.writeVal(value)
        else encodeNormalizedBigDecimal(decimal, value.abs, out)
      }

      /** Whether the value is a whole number play-json would write as plain digits, which is what
        * writing the unscaled value as a `Long` produces. Trailing zeros make no difference here:
        * stripping them only turns the scale negative, and the plain range turns it straight back.
        */
      private def isSmallWholeNumber(decimal: JavaBigDecimal): Boolean =
        decimal.scale == 0 && decimal.precision <= FastPathMostSignificantDigits

      /** Whether jsoniter's own `BigDecimal` writer already produces what play-json would, which
        * spares the value the normalization and limit checks below.
        *
        * Everything in this range renders plain — `scale <= precision + 5` is Java's rule for not
        * switching to an exponent — and magnitudes between 1e-6 and 1e18 sit inside the plain range
        * play-json writes plainly, so both write the same digits. Trailing zeros are the one thing
        * that would make them differ, and the unscaled value ending in a zero is what betrays them.
        */
      private def needsNoNormalization(decimal: JavaBigDecimal): Boolean = {
        val scale = decimal.scale
        scale > 0 && decimal.precision <= FastPathMostSignificantDigits &&
        scale <= decimal.precision + 5 &&
        decimal.unscaledValue.longValue % 10 != 0
      }

      private def encodeNormalizedBigDecimal(
          decimal: JavaBigDecimal,
          absolute: BigDecimal,
          out: JsonWriter
      ): Unit = {
        val stripped = stripTrailingZeros(decimal)
        val writePlain =
          absolute > bigDecimalSerializerSettings.minPlain && absolute < bigDecimalSerializerSettings.maxPlain
        // play-json renders the plain string back through Jackson, which writes any number as
        // `BigDecimal.toString`, so staying within the plain range only normalizes a negative scale
        val written = if (writePlain) stripped.setScale(Math.max(stripped.scale, 0)) else stripped

        if (written.scale == 0 && written.precision <= 18) out.writeVal(written.unscaledValue.longValue)
        else {
          val raw = written.toString

          val digits = countDigits(raw)
          if (digits >= bigDecimalParseSettings.digitsLimit) {
            out.encodeError(
              s"number of digits $digits exceeds the parse limit of ${bigDecimalParseSettings.digitsLimit}"
            )
          }

          val mathContext = bigDecimalParseSettings.mathContext
          val readBack = if (mathContext.getPrecision < digits) written.plus(mathContext) else written
          if (Math.abs(readBack.scale) >= bigDecimalParseSettings.scaleLimit) {
            out.encodeError(
              s"scale ${readBack.scale} exceeds the parse limit of ${bigDecimalParseSettings.scaleLimit}"
            )
          }

          out.writeRawVal(raw.getBytes(StandardCharsets.US_ASCII))
        }
      }

      private def stripTrailingZeros(value: JavaBigDecimal): JavaBigDecimal = {
        val stripped = value.stripTrailingZeros
        if (bigDecimalSerializerSettings.preserveZeroDecimal && value.scale > 0 && stripped.scale <= 0) {
          stripped.setScale(1)
        } else stripped
      }

      /** Counts digits the way `JsonReader` counts them while parsing, which is every digit of the
        * integer and fraction parts and none of the exponent. Leading and trailing zeros count too,
        * so `0.00000000015` is twelve digits rather than two — that is what makes the count
        * comparable to `digitsLimit`. `raw` comes from `BigDecimal.toString`, which separates the
        * exponent with an upper case `E`.
        */
      @tailrec
      private def countDigits(raw: String, index: Int = 0, digits: Int = 0): Int =
        if (index == raw.length) digits
        else {
          val char = raw.charAt(index)
          if (char == 'E') digits
          else countDigits(raw, index + 1, if (char >= '0' && char <= '9') digits + 1 else digits)
        }

      val nullValue: JsValue = JsNull
    }
}
