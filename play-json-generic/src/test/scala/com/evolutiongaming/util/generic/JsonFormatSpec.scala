package com.evolutiongaming.util.generic

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Format, JsObject, JsSuccess}

class JsonFormatSpec extends AnyWordSpec with Matchers {

  def check[T](o: T, json: JsObject)(implicit format: Format[T]): Assertion = {
    withClue(s"writing $o: ") { format.writes(o) shouldEqual json }
    withClue(s"reading $o: ") { format.reads(json) shouldEqual JsSuccess(o) }
  }
}
