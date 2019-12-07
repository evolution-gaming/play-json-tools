package com.evolutiongaming.playjsontools

import com.evolutiongaming.playjsontools.PlayJsonHelper._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsObject, JsSuccess, JsValue, Json, OFormat}

class TypeFormatSpec extends AnyFunSuite with Matchers {
  import TypeFormatSpec._

  private val mammalJson = Json.obj("type" -> "Mammal", "legs" -> 4)
  private val slothJson = Json.obj("type" -> "Sloth")

  implicit val animalFormat = new TypeFormat[Animal] {
    val mammalFormat = Json.format[Mammal]
    val slothFormat = OFormat.const(Sloth)

    def readsPf(json: JsValue): Pf = {
      case "Mammal" => mammalFormat.reads(json)
      case "Sloth"  => slothFormat.reads(json)
    }

    def writes(o: Animal): JsObject = o match {
      case x: Mammal => writes("Mammal", mammalFormat.writes(x))
      case Sloth => writes("Sloth", slothFormat.writes(Sloth))
    }
  }

  test("read case class") {
    animalFormat.reads(mammalJson) shouldEqual JsSuccess(Mammal(4))
  }

  test("write case class") {
    animalFormat.writes(Mammal(4)) shouldEqual mammalJson
  }

  test("read case object") {
    animalFormat.reads(slothJson) shouldEqual JsSuccess(Sloth)
  }

  test("write case object") {
    animalFormat.writes(Sloth) shouldEqual slothJson
  }
}

object TypeFormatSpec {
  sealed trait Animal
  final case class Mammal(legs: Int) extends Animal
  object Sloth extends Animal
}