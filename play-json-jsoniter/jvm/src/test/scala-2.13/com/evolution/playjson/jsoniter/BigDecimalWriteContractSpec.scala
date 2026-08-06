package com.evolution.playjson.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.JsonWriterException
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsNumber, JsValue, Json}

import java.util.Arrays
import scala.util.{Failure, Success, Try}

/**
 * States the BigDecimal write contract of [[PlayJsonJsoniter]] once, over a range of scales and
 * precisions, instead of over hand-picked examples: serialization either produces bytes that both
 * this codec and play-json read the same way, or it fails for a value that genuinely could not
 * have been read back.
 *
 * The limits themselves live in jsoniter's reader. Their exact boundaries are pinned by
 * `JsoniterBigDecimalRoundTripSpec`, which is what notices a jsoniter upgrade that moves one;
 * these properties cover the range in between. The number of samples is whatever the ScalaCheck
 * runner is configured with, so raise it with `-minSuccessfulTests` when changing the writer.
 */
object BigDecimalWriteContractSpec extends org.scalacheck.Properties("BigDecimalWriteContract") {

  private val genUnscaled: Gen[BigInt] =
    Gen.choose(1, 320).flatMap(digits => Gen.listOfN(digits, Gen.numChar).map(chars => BigInt(chars.mkString)))

  private val genBigDecimal: Gen[BigDecimal] = for {
    unscaled <- genUnscaled
    scale <- Gen.oneOf(Gen.choose(-7000, 7000), Gen.choose(-40, 40))
    negative <- Gen.oneOf(true, false)
  } yield BigDecimal(if (negative) -unscaled else unscaled, scale)

  implicit def generator: Arbitrary[BigDecimal] = Arbitrary(genBigDecimal)

  private def jsonOf(value: BigDecimal): JsValue = JsNumber(value)

  /**
   * play-json writes any BigDecimal, and by the parity property below its bytes are the ones this
   * writer would have produced, so they are the evidence of what a rejected value would have been.
   */
  private def playJsonBytesAreUnreadable(value: BigDecimal): Boolean =
    PlayJsonJsoniter.deserialize(Json.toBytes(jsonOf(value))).isFailure

  property("serialization either fails for good reason or produces bytes this codec can read") = forAll {
    (value: BigDecimal) =>
      Try(PlayJsonJsoniter.serialize(jsonOf(value))) match {
        case Failure(_: JsonWriterException) => playJsonBytesAreUnreadable(value)
        case Failure(_) => false
        case Success(bytes) => PlayJsonJsoniter.deserialize(bytes).isSuccess
      }
  }

  property("serialization either fails for good reason or produces the bytes play-json produces") = forAll {
    (value: BigDecimal) =>
      Try(PlayJsonJsoniter.serialize(jsonOf(value))) match {
        case Failure(_: JsonWriterException) => playJsonBytesAreUnreadable(value)
        case Failure(_) => false
        case Success(bytes) => Arrays.equals(bytes, Json.toBytes(jsonOf(value)))
      }
  }
}
