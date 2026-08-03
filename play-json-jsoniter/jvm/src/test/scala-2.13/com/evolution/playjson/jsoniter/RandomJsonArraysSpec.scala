package com.evolution.playjson.jsoniter

import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Test}
import play.api.libs.json.Json
import valuegen.RandomJsArrayGen

import java.util.Arrays

//sbt playJsonJsoniter/test:"runMain com.evolution.playjson.jsoniter.RandomJsonArraysSpec"
object RandomJsonArraysSpec extends org.scalacheck.Properties("RandomJsonSpec") {

  val Size = 5000

  //produces any imaginable Json array
  def randomArrayGen: Gen[value.JsArray] = RandomJsArrayGen()

  implicit def generator: Arbitrary[value.JsArray] = Arbitrary(randomArrayGen)

  override def overrideParameters(p: Test.Parameters): Test.Parameters =
    p.withMinSuccessfulTests(Size)

  property("Random json arrays") = forAll { (array: value.JsArray) =>
    val json = array.toString
    val jsValue = Json.parse(json)
    val bts = PlayJsonJsoniter.serialize(jsValue)
    val actJsValue = PlayJsonJsoniter.deserialize(bts)
    jsValue == actJsValue.get
  }

  property("Random json arrays: write using Jsoniter -> read using PlayJson") = forAll { (array: value.JsArray) =>
    val jsValue = Json.parse(array.toString)
    val bts = PlayJsonJsoniter.serialize(jsValue)
    jsValue == Json.parse(bts)
  }

  property("Random json arrays: Jsoniter and PlayJson write the same bytes") = forAll { (array: value.JsArray) =>
    val jsValue = Json.parse(array.toString)
    Arrays.equals(PlayJsonJsoniter.serialize(jsValue), Json.toBytes(jsValue))
  }
}
