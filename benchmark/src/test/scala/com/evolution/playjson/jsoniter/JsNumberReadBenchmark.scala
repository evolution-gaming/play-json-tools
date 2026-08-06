package com.evolution.playjson.jsoniter

import com.evolution.playjson.circe.PlayCirceAstConversions.circeToPlay
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromArray}
import io.circe.parser.{parse => parseCirce}
import org.openjdk.jmh.annotations.{Benchmark, Fork, Measurement, Scope, State, Warmup}
import org.openjdk.jmh.infra.Blackhole
import play.api.libs.json._

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * The read counterpart of [[JsNumberWriteBenchmark]], over the same payloads.
 *
 * To run: {{{sbt benchmark/Jmh/run com.evolution.playjson.jsoniter.JsNumberReadBenchmark}}}
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
class JsNumberReadBenchmark {

  private val parseSettings = JsonConfig.settings.bigDecimalParseConfig

  private val current: JsonValueCodec[JsValue] =
    JsonValueCodecJsValue(parseSettings, JsonConfig.settings.bigDecimalSerializerConfig)

  // written out as text rather than through a codec, so the payloads do not depend on the writer
  // being measured elsewhere
  private def arrayOf(values: Seq[BigDecimal]): Array[Byte] =
    values.mkString("[", ",", "]").getBytes(StandardCharsets.UTF_8)

  private val integers = arrayOf((1 to 1000).map(BigDecimal(_)))

  private val decimals = arrayOf((1 to 1000).map(index => BigDecimal(index) / 100))

  private val trailingZeros = arrayOf((1 to 1000).map(index => BigDecimal(index).setScale(4)))

  private val document = TestData.jsonBody.getBytes(StandardCharsets.UTF_8)

  // circe parses text, so it is given the same bytes as a string
  private val integersAsText = new String(integers, StandardCharsets.UTF_8)
  private val decimalsAsText = new String(decimals, StandardCharsets.UTF_8)
  private val trailingZerosAsText = new String(trailingZeros, StandardCharsets.UTF_8)
  private val documentAsText = new String(document, StandardCharsets.UTF_8)

  private def playViaCirce(text: String): JsValue =
    parseCirce(text).fold(throw _, circeToPlay)

  @Benchmark def integersCurrent(hole: Blackhole): Unit = hole.consume(readFromArray(integers)(current))
  @Benchmark def integersPlayJson(hole: Blackhole): Unit = hole.consume(Json.parse(integers))
  @Benchmark def integersCirce(hole: Blackhole): Unit = hole.consume(parseCirce(integersAsText))
  @Benchmark def integersPlayCirce(hole: Blackhole): Unit = hole.consume(playViaCirce(integersAsText))

  @Benchmark def decimalsCurrent(hole: Blackhole): Unit = hole.consume(readFromArray(decimals)(current))
  @Benchmark def decimalsPlayJson(hole: Blackhole): Unit = hole.consume(Json.parse(decimals))
  @Benchmark def decimalsCirce(hole: Blackhole): Unit = hole.consume(parseCirce(decimalsAsText))
  @Benchmark def decimalsPlayCirce(hole: Blackhole): Unit = hole.consume(playViaCirce(decimalsAsText))

  @Benchmark def trailingZerosCurrent(hole: Blackhole): Unit = hole.consume(readFromArray(trailingZeros)(current))
  @Benchmark def trailingZerosPlayJson(hole: Blackhole): Unit = hole.consume(Json.parse(trailingZeros))
  @Benchmark def trailingZerosCirce(hole: Blackhole): Unit = hole.consume(parseCirce(trailingZerosAsText))
  @Benchmark def trailingZerosPlayCirce(hole: Blackhole): Unit = hole.consume(playViaCirce(trailingZerosAsText))

  @Benchmark def documentCurrent(hole: Blackhole): Unit = hole.consume(readFromArray(document)(current))
  @Benchmark def documentPlayJson(hole: Blackhole): Unit = hole.consume(Json.parse(document))
  @Benchmark def documentCirce(hole: Blackhole): Unit = hole.consume(parseCirce(documentAsText))
  @Benchmark def documentPlayCirce(hole: Blackhole): Unit = hole.consume(playViaCirce(documentAsText))
}
