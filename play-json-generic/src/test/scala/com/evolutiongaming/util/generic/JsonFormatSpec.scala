package com.evolutiongaming.util.generic

import org.scalatest.{Assertion, Matchers, WordSpec}
import play.api.libs.json.{Format, JsObject, JsSuccess}

class JsonFormatSpec extends WordSpec with Matchers {

  def check[T](o: T, json: JsObject)(implicit format: Format[T]): Assertion = {
    withClue(s"writing $o: ") { format.writes(o) shouldEqual json }
    withClue(s"reading $o: ") { format.reads(json) shouldEqual JsSuccess(o) }
  }
}
