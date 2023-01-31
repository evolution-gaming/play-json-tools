package com.evolutiongaming.util

import com.evolutiongaming.util.PlayCirceAstConversions._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.libs.json.{JsObject, Json}

class PlayCirceAstConversionsStackSafetySpec extends AnyFreeSpec with TableDrivenPropertyChecks with Matchers {
  "Play to Circe AST conversion" - {
    "can handle deeply nested structures" in {
      val playJson = (1 to 100000).foldLeft(JsObject.empty)((o, _) => Json.obj("n" -> o))
      assert(playToCirce(playJson).isObject)
    }

    "can handle long arrays" in {
      val playJson = Json.arr(1 to 100000)
      assert(playToCirce(playJson).isArray)
    }
  }
}
