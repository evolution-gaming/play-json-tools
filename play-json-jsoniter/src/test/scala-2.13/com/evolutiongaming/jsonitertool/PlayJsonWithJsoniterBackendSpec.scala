package com.evolutiongaming.jsonitertool

import com.evolutiongaming.jsonitertool.TestDataGenerators.{User, genUser}
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Test}
import play.api.libs.json.Json

//sbt playJsonJsoniter/test:"runMain com.evolutiongaming.jsonitertool.PlayJsonWithJsoniterBackendSpec"
object PlayJsonWithJsoniterBackendSpec extends org.scalacheck.Properties("PlayJsonWithJsoniterBackend") {
  val Size = 200

  override def overrideParameters(p: Test.Parameters): Test.Parameters =
    p.withMinSuccessfulTests(Size)

  implicit def generator: Arbitrary[User] = Arbitrary(genUser)

  property("Write using PlayJson -> Read using Jsoniter") = forAll { user: User =>
    val jsValue = Json.toJson(user)
    val bts = Json.toBytes(jsValue)
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
