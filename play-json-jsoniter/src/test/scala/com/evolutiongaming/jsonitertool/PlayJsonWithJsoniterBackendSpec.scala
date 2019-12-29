package com.evolutiongaming.jsonitertool

import TestDataGenerators._
import org.scalacheck.Prop._
import play.api.libs.json.Json
import org.scalacheck.{Arbitrary, Gen, Test}

//sbt playJsonJsoniter/test:"runMain com.evolutiongaming.jsonitertool.PlayJsonWithJsoniterBackendSpec"
object PlayJsonWithJsoniterBackendSpec extends org.scalacheck.Properties("PlayJsonWithJsoniterBackend") {
  val Size = 200

  override def overrideParameters(p: Test.Parameters): Test.Parameters =
    p.withMinSuccessfulTests(Size)

  implicit def generator: Arbitrary[User] = Arbitrary(genUser)

  property("Write using PlayJson -> Read using Jsoniter") = forAll { user: User =>
    val jsValue = Json.toJson(user)
    val bts = Json.toBytes(jsValue)
    //println(s"${line.name}: ${bts.size / 1024} kb")
    val actJsValue = PlayJsonJsoniter.deserialize(bts)
    user == Json.fromJson[User](actJsValue).get
  }

  property("Write using PlayJson -> Read using Jsoniter. Batch") = forAll(
    Gen.containerOfN[Vector, User](Size, genUser),
  ) { batch: Vector[User] =>
    val bools = batch.map { user =>
      val jsValue = Json.toJson(user)
      val bts = Json.toBytes(jsValue)
      val actJsValue = PlayJsonJsoniter.deserialize(bts)
      user == Json.fromJson[User](actJsValue).get
    }

    bools.find(_ == false).isEmpty
  }

  property("Write using Jsoniter -> Read using Jsoniter. Batch") = forAll(
    Gen.containerOfN[Vector, User](Size, genUser),
  ) { batch: Vector[User] =>
    val bools = batch.map { user =>
      val jsValue = Json.toJson(user)
      val bts = PlayJsonJsoniter.serialize(jsValue)
      val actJsValue = PlayJsonJsoniter.deserialize(bts)
      user == Json.fromJson[User](actJsValue).get
    }

    bools.find(_ == false).isEmpty
  }
}
