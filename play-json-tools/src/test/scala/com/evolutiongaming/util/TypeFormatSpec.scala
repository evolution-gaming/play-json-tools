package com.evolutiongaming.util

import com.evolutiongaming.util.JsonFormats.{TypeFormat, const}
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.{JsObject, JsValue, Json}

class TypeFormatSpec extends FunSuite with Matchers {
  private val mammalJson = Json.obj("type" -> "Mammal", "legs" -> 4)
  private val slothJson = Json.obj("type" -> "Sloth")

  implicit val animalFormat = new TypeFormat[Animal] {
    val MammalFormat = Json.format[Mammal]
    val SlothFormat = const(Sloth)

    def readsPf(json: JsValue): Pf = {
      case "Mammal" => MammalFormat.reads(json)
      case "Sloth"  => SlothFormat.reads(json)
    }

    def writes(o: Animal): JsObject = o match {
      case x: Mammal => writes("Mammal", MammalFormat.writes(x))
      case Sloth => writes("Sloth", SlothFormat.writes(Sloth))
    }
  }

  test("read case class") {
    Json.fromJson[Animal](mammalJson).get shouldEqual Mammal(4)
  }

  test("write case class") {
    Json.toJson(Mammal(4)) shouldEqual mammalJson
  }

  test("read case object") {
    Json.fromJson[Animal](slothJson).get shouldEqual Sloth
  }

  test("write case object") {
    Json.toJson(Sloth) shouldEqual slothJson
  }

  sealed trait Animal
  final case class Mammal(legs: Int) extends Animal
  object Sloth extends Animal
}