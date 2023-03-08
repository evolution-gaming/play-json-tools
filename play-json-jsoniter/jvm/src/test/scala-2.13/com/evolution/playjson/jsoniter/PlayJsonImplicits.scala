package com.evolution.playjson.jsoniter

import com.evolution.playjson.jsoniter.TestDataGenerators.{Address, User}
import play.api.libs.json.Json

trait PlayJsonImplicits {

  implicit val e = Json.reads[Address]
  implicit val f = Json.writes[Address]

  implicit val g = Json.reads[User]
  implicit val h = Json.writes[User]

}
