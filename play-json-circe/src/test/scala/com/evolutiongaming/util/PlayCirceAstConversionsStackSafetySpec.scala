package com.evolutiongaming.util

import com.evolutiongaming.util.PlayCirceAstConversions._
import io.circe.{Json => CirceJson}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.{json => PlayJson}

class PlayCirceAstConversionsStackSafetySpec extends AnyFreeSpec with Matchers {
  "Play to Circe AST conversion" - {
    "can handle deeply nested structures" in {
      val playJson = (1 to 100000).foldLeft(PlayJson.JsObject.empty)((o, _) => PlayJson.Json.obj("n" -> o))
      assert(playToCirce(playJson).isObject)
    }

    "can handle long arrays" in {
      val playJson = PlayJson.Json.arr(1 to 100000)
      assert(playToCirce(playJson).isArray)
    }
  }

  "Circe to Play AST conversion" - {
    "can handle deeply nested structures" in {
      val circeJson = (1 to 100000).foldLeft(CirceJson.obj())((o, _) => CirceJson.obj("n" -> o))
      assert(circeToPlay(circeJson).isInstanceOf[PlayJson.JsObject])
    }

    "can handle long arrays" in {
      val playJson = CirceJson.fromValues((1 to 100000).map(CirceJson.fromInt))
      assert(circeToPlay(playJson).isInstanceOf[PlayJson.JsArray])
    }
  }
}
