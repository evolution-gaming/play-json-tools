package com.evolution.playjson.jsoniter

import com.evolution.playjson.circe.PlayCirceAstConversions.playToCirce
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, writeToArray}
import io.circe.{Json => CirceJson}
import org.openjdk.jmh.annotations.{Benchmark, Fork, Measurement, Scope, State, Warmup}
import org.openjdk.jmh.infra.Blackhole
import play.api.libs.json._

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Throughput of turning a play-json `JsValue` into bytes, over payloads that exercise the
 * `JsNumber` branch of `encodeValue`, with play-json and circe as reference points.
 *
 * To run: {{{sbt benchmark/Jmh/run com.evolution.playjson.jsoniter.JsNumberWriteBenchmark}}}
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
class JsNumberWriteBenchmark {

  private val parseSettings = JsonConfig.settings.bigDecimalParseConfig

  private implicit val current: JsonValueCodec[JsValue] =
    JsonValueCodecJsValue(parseSettings, JsonConfig.settings.bigDecimalSerializerConfig)

  private def arrayOf(values: Seq[BigDecimal]): JsValue = JsArray(values.map(JsNumber(_)))

  // hits the scale == 0 fast path
  private val integers = arrayOf((1 to 1000).map(BigDecimal(_)))

  // two decimal places, the shape money tends to have
  private val decimals = arrayOf((1 to 1000).map(index => BigDecimal(index) / 100))

  // trailing zeros, so the value has to be normalized before it can be written
  private val trailingZeros = arrayOf((1 to 1000).map(index => BigDecimal(index).setScale(4)))

  private val document = Json.parse(TestData.jsonBody)

  private def circeBytes(circeJson: CirceJson): Array[Byte] =
    circeJson.noSpaces.getBytes(StandardCharsets.UTF_8)

  // circe writing a value it already holds, against the same value reached through play-json-circe
  private val integersAsCirce = playToCirce(integers)
  private val decimalsAsCirce = playToCirce(decimals)
  private val trailingZerosAsCirce = playToCirce(trailingZeros)
  private val documentAsCirce = playToCirce(document)

  @Benchmark def integersCurrent(hole: Blackhole): Unit = hole.consume(writeToArray(integers)(current))
  @Benchmark def integersPlayJson(hole: Blackhole): Unit = hole.consume(Json.toBytes(integers))
  @Benchmark def integersCirce(hole: Blackhole): Unit = hole.consume(circeBytes(integersAsCirce))
  @Benchmark def integersPlayCirce(hole: Blackhole): Unit = hole.consume(circeBytes(playToCirce(integers)))

  @Benchmark def decimalsCurrent(hole: Blackhole): Unit = hole.consume(writeToArray(decimals)(current))
  @Benchmark def decimalsPlayJson(hole: Blackhole): Unit = hole.consume(Json.toBytes(decimals))
  @Benchmark def decimalsCirce(hole: Blackhole): Unit = hole.consume(circeBytes(decimalsAsCirce))
  @Benchmark def decimalsPlayCirce(hole: Blackhole): Unit = hole.consume(circeBytes(playToCirce(decimals)))

  @Benchmark def trailingZerosCurrent(hole: Blackhole): Unit = hole.consume(writeToArray(trailingZeros)(current))
  @Benchmark def trailingZerosPlayJson(hole: Blackhole): Unit = hole.consume(Json.toBytes(trailingZeros))
  @Benchmark def trailingZerosCirce(hole: Blackhole): Unit = hole.consume(circeBytes(trailingZerosAsCirce))
  @Benchmark def trailingZerosPlayCirce(hole: Blackhole): Unit =
    hole.consume(circeBytes(playToCirce(trailingZeros)))

  @Benchmark def documentCurrent(hole: Blackhole): Unit = hole.consume(writeToArray(document)(current))
  @Benchmark def documentPlayJson(hole: Blackhole): Unit = hole.consume(Json.toBytes(document))
  @Benchmark def documentCirce(hole: Blackhole): Unit = hole.consume(circeBytes(documentAsCirce))
  @Benchmark def documentPlayCirce(hole: Blackhole): Unit = hole.consume(circeBytes(playToCirce(document)))
}
