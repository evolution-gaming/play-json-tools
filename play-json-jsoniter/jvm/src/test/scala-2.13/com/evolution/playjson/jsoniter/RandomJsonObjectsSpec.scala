package com.evolution.playjson.jsoniter

import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Test}
import play.api.libs.json.Json
import valuegen.RandomJsObjGen

import java.util.Arrays

//sbt playJsonJsoniter/test:"runMain com.evolution.playjson.jsoniter.RandomJsonObjectsSpec"
object RandomJsonObjectsSpec extends org.scalacheck.Properties("RandomJsonObjectsSpec") {

  val Size = 5000

  // produces any imaginable Json object
  def randomObjGen: Gen[value.JsObj] = RandomJsObjGen()

  implicit def generator: Arbitrary[value.JsObj] = Arbitrary(randomObjGen)

  override def overrideParameters(p: Test.Parameters): Test.Parameters =
    p.withMinSuccessfulTests(Size)

  property("Random json objects") = forAll { (obj: value.JsObj) =>
    val json = obj.toString
    val jsValue = Json.parse(json)
    val bts = PlayJsonJsoniter.serialize(jsValue)
    val actJsValue = PlayJsonJsoniter.deserialize(bts)
    jsValue == actJsValue.get
  }

  property("Random json objects: write using Jsoniter -> read using PlayJson") = forAll { (obj: value.JsObj) =>
    val jsValue = Json.parse(obj.toString)
    val bts = PlayJsonJsoniter.serialize(jsValue)
    jsValue == Json.parse(bts)
  }

  property("Random json objects: Jsoniter and PlayJson write the same bytes") = forAll { (obj: value.JsObj) =>
    val jsValue = Json.parse(obj.toString)
    Arrays.equals(PlayJsonJsoniter.serialize(jsValue), Json.toBytes(jsValue))
  }
}
