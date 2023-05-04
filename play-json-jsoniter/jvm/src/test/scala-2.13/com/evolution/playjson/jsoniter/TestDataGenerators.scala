package com.evolution.playjson.jsoniter

import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.Json
import value.JsObj
import valuegen.Preamble._
import valuegen.{JsArrayGen, JsObjGen}

object TestDataGenerators extends PlayJsonImplicits {

  val ALPHABET: Vector[String] = "abcdefghijklmnopqrstuvwzyz".split("").toVector

  def str(len: Int): Gen[String] = Gen.listOfN(len, Gen.choose(0, ALPHABET.size - 1).map(ALPHABET(_))).map(_.mkString(""))

  def flagGen: Gen[Boolean] = Arbitrary.arbitrary[Boolean]

  def nameGen: Gen[String] = Gen.choose(5, 20).flatMap(str)

  def birthDateGen: Gen[String] = Gen.choose(5, 20).flatMap(str)

  def latitudeGen: Gen[Double] = Arbitrary.arbitrary[Double]

  def longitudeGen: Gen[Double] = Arbitrary.arbitrary[Double]

  def longGen: Gen[Long] = Arbitrary.arbitrary[Long]

  def ints: Gen[Int] = Arbitrary.arbitrary[Int]

  def emailGen: Gen[String] = Gen.choose(5, 20).flatMap(str)

  def countryGen: Gen[String] = Gen.choose(5, 20).flatMap(str)

  def friendGen(depth: Int): Gen[JsObj] = JsObjGen(
    "name" -> nameGen,
    "email" -> emailGen,
    "flag" -> flagGen,
    "l" -> longGen,
    "from" -> JsObjGen("country" -> countryGen, "doubles" -> JsArrayGen(latitudeGen, longitudeGen)),
    "metrics" -> JsArrayGen(ints, ints),
    "friends" -> (if (depth > 0) JsArrayGen(friendGen(depth - 1)) else JsArrayGen())
  )

  def objGen: Gen[JsObj] = JsObjGen(
    "name" -> nameGen,
    "email" -> emailGen,
    "flag" -> flagGen,
    "l" -> longGen,
    "from" -> JsObjGen("country" -> countryGen, "doubles" -> JsArrayGen(latitudeGen, longitudeGen)),
    "metrics" -> JsArrayGen(ints, ints, ints, ints, ints),
    "friends" -> JsArrayGen(friendGen(depth = 50))
  )

  def genUser: Gen[User] = objGen
    .map(json => Json.fromJson[User](Json.parse(json.toString)).get)

  case class Address(country: String, doubles: List[Double])

  case class User(name: String, email: String, flag: Boolean, from: Address, l: Long, metrics: List[Int],
    friends: List[User])
}
