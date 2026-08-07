package com.evolution.playjson.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.{
  JsonReaderException,
  JsonValueCodec,
  JsonWriterException,
  readFromArray,
  writeToArray
}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

import java.nio.charset.StandardCharsets
import scala.util.{Success, Try}

/** Nesting is bounded in both directions, at the depth play-json reads. Reading further diverges
  * from play-json and eventually exhausts the stack. Writing further produces a document play-json
  * cannot read, which is the one place this codec is deliberately stricter than play-json.
  *
  * JVM only on purpose: the limit play-json enforces is Jackson's, and Jackson is the JVM backend.
  */
class JsonDepthSpec extends AnyFunSuite with Matchers {

  /** Jackson's default, and therefore what play-json reads. */
  private val playJsonNestingLimit = 1000

  private def nestedObjectBytes(depth: Int): Array[Byte] =
    asBytes(("{\"n\":" * depth) + "1" + ("}" * depth))

  private def nestedArrayBytes(depth: Int): Array[Byte] =
    asBytes(("[" * depth) + "1" + ("]" * depth))

  private def asBytes(text: String): Array[Byte] = text.getBytes(StandardCharsets.UTF_8)

  private def wrapping(layers: Int, innermost: String): Array[Byte] =
    asBytes(("{\"n\":" * layers) + innermost + ("}" * layers))

  private def nestedAround(innermost: JsValue, layers: Int): JsValue = (1 to layers).foldLeft(innermost) { (inner, _) =>
    JsObject(Seq("n" -> inner))
  }

  private def nestedValue(depth: Int): JsValue = (1 to depth).foldLeft[JsValue](JsNumber(1)) { (inner, _) =>
    JsObject(Seq("n" -> inner))
  }

  private def codecOf(maxNestingDepth: Int): Either[String, JsonValueCodec[JsValue]] =
    JsonValueCodecJsValue.of(
      JsonConfig.settings.bigDecimalParseConfig,
      JsonConfig.settings.bigDecimalSerializerConfig,
      maxNestingDepth
    )

  test("a shallowly nested document round-trips") {
    val value = nestedValue(64)
    PlayJsonJsoniter.deserialize(PlayJsonJsoniter.serialize(value)) shouldEqual Success(value)
  }

  test("reading an object nested past the stack depth fails") {
    PlayJsonJsoniter.deserialize(nestedObjectBytes(100000)).isFailure shouldBe true
  }

  test("reading an array nested past the stack depth fails") {
    PlayJsonJsoniter.deserialize(nestedArrayBytes(100000)).isFailure shouldBe true
  }

  test("reading accepts the same nesting play-json accepts") {
    PlayJsonJsoniter.deserialize(nestedObjectBytes(playJsonNestingLimit)).isSuccess shouldBe true
  }

  test("reading refuses the nesting play-json refuses") {
    PlayJsonJsoniter.deserialize(nestedObjectBytes(playJsonNestingLimit + 1)).isFailure shouldBe true
  }

  test("reading agrees with play-json around the limit") {
    val outcomes = (playJsonNestingLimit - 2 to playJsonNestingLimit + 2).map { depth =>
      val json = nestedObjectBytes(depth)
      val playJsonReads = Try(Json.parse(json)).isSuccess
      depth -> (PlayJsonJsoniter.deserialize(json).isSuccess, playJsonReads)
    }

    outcomes.filter { case (_, (jsoniter, playJson)) => jsoniter != playJson } shouldBe empty
  }

  test("writing a value nested deeper than it could be read is refused") {
    a[JsonWriterException] should be thrownBy PlayJsonJsoniter.serialize(nestedValue(playJsonNestingLimit + 1))
  }

  test("the default limit is the one play-json reads") {
    JsonValueCodecJsValue.DefaultMaxNestingDepth shouldEqual playJsonNestingLimit
  }

  test("a codec given a smaller limit holds both directions to it") {
    val shallow = 3
    implicit val codec: JsonValueCodec[JsValue] =
      codecOf(shallow).getOrElse(fail(s"could not build a codec bounded at $shallow"))

    readFromArray[JsValue](nestedObjectBytes(shallow)) shouldEqual nestedValue(shallow)
    a[JsonReaderException] should be thrownBy readFromArray[JsValue](nestedObjectBytes(shallow + 1))

    writeToArray[JsValue](nestedValue(shallow)) shouldEqual nestedObjectBytes(shallow)
    a[JsonWriterException] should be thrownBy writeToArray[JsValue](nestedValue(shallow + 1))
  }

  test("a limit that refuses everything is reported rather than built") {
    Seq(0, -1, Int.MinValue).foreach { limit =>
      withClue(s"limit $limit: ") {
        codecOf(limit).left.map(_.contains(limit.toString)) shouldEqual Left(true)
      }
    }
  }

  test("the default limit builds a codec") {
    codecOf(JsonValueCodecJsValue.DefaultMaxNestingDepth).isRight shouldBe true
  }

  test("an empty container at the limit counts as a level, as it does for play-json") {
    Seq("{}", "[]").foreach { innermost =>
      // the innermost container brings the total to the depth in the clue
      val atLimit = wrapping(playJsonNestingLimit - 1, innermost)
      val pastLimit = wrapping(playJsonNestingLimit, innermost)

      withClue(s"$innermost at depth $playJsonNestingLimit: ") {
        PlayJsonJsoniter.deserialize(atLimit).isSuccess shouldBe true
        Try(Json.parse(atLimit)).isSuccess shouldBe true
      }

      withClue(s"$innermost at depth ${playJsonNestingLimit + 1}: ") {
        PlayJsonJsoniter.deserialize(pastLimit).isFailure shouldBe true
        Try(Json.parse(pastLimit)).isSuccess shouldBe false
      }
    }
  }

  test("writing a value whose innermost container is empty counts it as a level") {
    val atLimit = nestedAround(JsObject.empty, playJsonNestingLimit - 1)
    val pastLimit = nestedAround(JsObject.empty, playJsonNestingLimit)

    PlayJsonJsoniter.deserialize(PlayJsonJsoniter.serialize(atLimit)) shouldEqual Success(atLimit)
    a[JsonWriterException] should be thrownBy PlayJsonJsoniter.serialize(pastLimit)
  }

  test("arrays and objects count towards the same limit") {
    val mixed = (1 to playJsonNestingLimit / 2).foldLeft[JsValue](JsNumber(1)) { (inner, _) =>
      JsArray(Seq(JsObject(Seq("n" -> inner))))
    }

    PlayJsonJsoniter.deserialize(PlayJsonJsoniter.serialize(mixed)) shouldEqual Success(mixed)
    a[JsonWriterException] should be thrownBy PlayJsonJsoniter.serialize(JsArray(Seq(mixed)))
  }
}
