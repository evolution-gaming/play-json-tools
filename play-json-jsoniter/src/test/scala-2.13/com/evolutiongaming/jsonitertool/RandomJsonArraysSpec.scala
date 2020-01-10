package com.evolutiongaming.jsonitertool

import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Test}
import play.api.libs.json.Json
import valuegen.RandomJsArrayGen

//sbt playJsonJsoniter/test:"runMain com.evolutiongaming.jsonitertool.RandomJsonArraysSpec"
object RandomJsonArraysSpec extends org.scalacheck.Properties("RandomJsonSpec") {

  val Size = 5000

  //produces any imaginable Json array
  def randomArrayGen: Gen[value.JsArray] = RandomJsArrayGen()

  implicit def generator: Arbitrary[value.JsArray] = Arbitrary(randomArrayGen)

  override def overrideParameters(p: Test.Parameters): Test.Parameters =
    p.withMinSuccessfulTests(Size)

  property("Random json arrays") = forAll { array: value.JsArray =>
    val json = array.toString
    val jsValue = Json.parse(json)
    val bts = PlayJsonJsoniter.serialize(jsValue)
    val actJsValue = PlayJsonJsoniter.deserialize(bts)
    jsValue == actJsValue.get
  }
}
