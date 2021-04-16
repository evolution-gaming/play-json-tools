package com.evolutiongaming.jsonitertool

import play.api.libs.json.Json
import com.evolutiongaming.jsonitertool.TestDataGenerators.{Address, User}

trait PlayJsonImplicits {

  implicit val e = Json.reads[Address]
  implicit val f = Json.writes[Address]

  implicit val g = Json.reads[User]
  implicit val h = Json.writes[User]

}
