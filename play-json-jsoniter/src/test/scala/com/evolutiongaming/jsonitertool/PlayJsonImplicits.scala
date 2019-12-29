package com.evolutiongaming.jsonitertool

import com.evolutiongaming.jsonitertool.TestDataGenerators.{Address, DataLine, Friend, User}
import play.api.libs.json.Json

trait PlayJsonImplicits {

  implicit val a = Json.reads[Friend]
  implicit val b = Json.writes[Friend]

  implicit val c = Json.reads[DataLine]
  implicit val d = Json.writes[DataLine]

  implicit val e = Json.reads[Address]
  implicit val f = Json.writes[Address]

  implicit val g = Json.reads[User]
  implicit val h = Json.writes[User]

}
