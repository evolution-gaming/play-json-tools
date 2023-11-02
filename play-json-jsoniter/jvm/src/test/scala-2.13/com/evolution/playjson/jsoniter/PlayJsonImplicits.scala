package com.evolution.playjson.jsoniter

import com.evolution.playjson.jsoniter.TestDataGenerators.{Address, User}
import play.api.libs.json.{Json, OWrites, Reads}

trait PlayJsonImplicits {

  implicit val e: Reads[Address] = Json.reads[Address]
  implicit val f: OWrites[Address] = Json.writes[Address]

  implicit val g: Reads[User] = Json.reads[User]
  implicit val h: OWrites[User] = Json.writes[User]

}
