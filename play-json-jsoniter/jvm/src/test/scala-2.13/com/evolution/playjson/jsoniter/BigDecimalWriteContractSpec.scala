package com.evolution.playjson.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.JsonWriterException
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Test}
import play.api.libs.json.{JsNumber, JsValue, Json}

import java.util.Arrays
import scala.util.{Failure, Success, Try}

/**
 * States the BigDecimal write contract of [[PlayJsonJsoniter]] once, over the whole range of
 * scales and precisions the reader has limits for, instead of over hand-picked examples:
 * serialization either fails, or produces bytes that both this codec and play-json can read.
 *
 * The limits themselves live in jsoniter's reader, so these properties are what notices if a
 * jsoniter upgrade moves a boundary the writer mirrors.
 */
object BigDecimalWriteContractSpec extends org.scalacheck.Properties("BigDecimalWriteContract") {

  val Size = 5000

  override def overrideParameters(p: Test.Parameters): Test.Parameters =
    p.withMinSuccessfulTests(Size)

  // covers both sides of digitsLimit (310) and scaleLimit (6178)
  private val genUnscaled: Gen[BigInt] =
    Gen.choose(1, 320).flatMap(digits => Gen.listOfN(digits, Gen.numChar).map(chars => BigInt(chars.mkString)))

  private val genBigDecimal: Gen[BigDecimal] = for {
    unscaled <- genUnscaled
    scale <- Gen.oneOf(Gen.choose(-7000, 7000), Gen.choose(-40, 40))
    negative <- Gen.oneOf(true, false)
  } yield BigDecimal(if (negative) -unscaled else unscaled, scale)

  implicit def generator: Arbitrary[BigDecimal] = Arbitrary(genBigDecimal)

  private def jsonOf(value: BigDecimal): JsValue = JsNumber(value)

  property("serialization either fails or produces bytes this codec can read") = forAll {
    (value: BigDecimal) =>
      Try(PlayJsonJsoniter.serialize(jsonOf(value))) match {
        case Failure(_: JsonWriterException) => true
        case Failure(_) => false
        case Success(bytes) => PlayJsonJsoniter.deserialize(bytes).isSuccess
      }
  }

  property("serialization either fails or produces the bytes play-json produces") = forAll {
    (value: BigDecimal) =>
      Try(PlayJsonJsoniter.serialize(jsonOf(value))) match {
        case Failure(_: JsonWriterException) => true
        case Failure(_) => false
        case Success(bytes) => Arrays.equals(bytes, Json.toBytes(jsonOf(value)))
      }
  }
}
