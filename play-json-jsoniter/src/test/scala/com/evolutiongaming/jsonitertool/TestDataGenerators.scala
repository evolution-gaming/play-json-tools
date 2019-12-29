package com.evolutiongaming.jsonitertool

import java.util.concurrent.ThreadLocalRandom

import play.api.libs.json.Json
import valuegen.Preamble._
import valuegen.{JsArrayGen, JsObjGen}
import value.JsObj
import org.scalacheck.{Arbitrary, Gen}

object TestDataGenerators extends PlayJsonImplicits {

  val ALPHABET: Vector[String] = "abcdefghijklmnopqrstuvwzyz".split("").toVector

  def str(len: Int): Gen[String] = Gen.listOfN(len, Gen.choose(0, ALPHABET.size - 1).map(ALPHABET(_))).map(_.mkString(""))

  def flagGen: Gen[Boolean] = Arbitrary.arbitrary[Boolean]

  def nameGen: Gen[String] = str(ThreadLocalRandom.current.nextInt(5, 20))

  def birthDateGen: Gen[String] = str(ThreadLocalRandom.current.nextInt(5, 20))

  def latitudeGen: Gen[Double] = Arbitrary.arbitrary[Double]

  def longitudeGen: Gen[Double] = Arbitrary.arbitrary[Double]

  def longGen: Gen[Long] = Arbitrary.arbitrary[Long]

  def ints: Gen[Int] = Arbitrary.arbitrary[Int]

  def emailGen: Gen[String] = str(ThreadLocalRandom.current.nextInt(5, 20))

  def countryGen: Gen[String] = str(ThreadLocalRandom.current.nextInt(5, 20))

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

  case class Friend(id: Int, name: String)

  case class DataLine(
    id: String,
    index: Int,
    guid: String,
    isActive: Boolean,
    balance: String,
    picture: String,
    age: Int,
    eyeColor: String,
    name: String,
    gender: String,
    company: String,
    phone: String,
    address: String,
    about: String,
    registered: String,
    latitude: Double,
    longitude: Double,
    tags: List[String],
    friends: List[Friend],
    greeting: String,
    favoriteFruit: String
  )


  val jsonBody =
    """
      |  {
      |    "id": "576278df40bd978bc65ddb06",
      |    "index": 0,
      |    "guid": "e4c30481-84f3-4193-a83c-fe0435348774",
      |    "isActive": true,
      |    "balance": "$1,355.27",
      |    "picture": "http://placehold.it/32x32",
      |    "age": 26,
      |    "eyeColor": "green",
      |    "name": "Stewart Navarro",
      |    "gender": "male",
      |    "company": "NORSUP",
      |    "phone": "+1 (916) 543-2895",
      |    "address": "133 Stillwell Place, wrtywrt",
      |    "about": "Eiusmod excepteur do esse minim nisi occaecat enim non dolor labore ipsum ut. Fugiat deserunt est pariatur pariatur. Laboris aute cillum tempor in exercitation laboris laboris fugiat velit enim ut ad. Ea labore commodo consectetur ut anim anim sint consectetur commodo.\r\n",
      |    "registered": "2015-05-01T05:24:06 -10:00",
      |    "latitude": -29.132033,
      |    "longitude": -58.249295,
      |    "tags": [
      |      "sunt",
      |      "Lorem",
      |      "adipisicing",
      |      "duis",
      |      "nulla",
      |      "sint",
      |      "fugiat"
      |    ],
      |    "friends": [
      |      {
      |        "id": 0,
      |        "name": "Yates Pruitt"
      |      },
      |      {
      |        "id": 1,
      |        "name": "Chen Henry"
      |      },
      |      {
      |        "id": 2,
      |        "name": "Miranda Vincent"
      |      }
      |    ],
      |    "greeting": "Hello, Stewart Navarro! You have 7 unread messages",
      |    "favoriteFruit": "strawberry"
      |  }
        """.stripMargin

}