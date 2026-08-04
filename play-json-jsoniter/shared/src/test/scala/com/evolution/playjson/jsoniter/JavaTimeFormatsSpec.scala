package com.evolution.playjson.jsoniter

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsError, JsNumber, JsString, JsSuccess}

import java.time._

/**
  * Contract tests for the java.time formats of [[PlayJsonJsoniter]]: every format writes a value in
  * a pinned form and reads it back, and a value it cannot read is reported against the name of the
  * type it was asked to read.
  */
class JavaTimeFormatsSpec extends AnyFunSuite with Matchers {

  import PlayJsonJsoniter._

  private def assertContract[A](name: String, value: A, written: String)(implicit format: Format[A]): Unit = {
    assertRoundTrip(value, written)(format)
    assertReadErrorNames(name, written)(format)
  }

  /**
    * Pins the written form as well as the round trip: a change of codec that still round-tripped
    * would go unnoticed otherwise, and the written form is what every other reader of the wire sees.
    */
  private def assertRoundTrip[A](value: A, written: String)(implicit format: Format[A]): Unit = {
    format.writes(value) shouldEqual JsString(written)
    format.reads(JsString(written)) shouldEqual JsSuccess(value)
    ()
  }

  private def assertReadErrorNames[A](name: String, written: String)(implicit format: Format[A]): Unit = {
    format.reads(JsString("not a " + name)) shouldEqual JsError(name)
    format.reads(JsNumber(1)) shouldEqual JsError(name)
    format.reads(JsString(aboveAscii(written))) shouldEqual JsError(name)
    ()
  }

  /**
    * The written form with every character moved out of ASCII, its low byte unchanged. The reader
    * narrows a character at a time into a byte buffer, so these would read back as the value they
    * were derived from if it did not check the high bits itself.
    */
  private def aboveAscii(written: String): String =
    written.map(character => (character + 0x100).toChar)

  test("Duration") { assertContract("Duration", Duration.ofSeconds(90), "PT1M30S") }
  test("Instant") { assertContract("Instant", Instant.parse("2026-08-03T10:15:30Z"), "2026-08-03T10:15:30Z") }
  test("LocalDate") { assertContract("LocalDate", LocalDate.of(2026, 8, 3), "2026-08-03") }
  test("LocalDateTime") {
    assertContract("LocalDateTime", LocalDateTime.of(2026, 8, 3, 10, 15, 30), "2026-08-03T10:15:30")
  }
  test("LocalTime") { assertContract("LocalTime", LocalTime.of(10, 15, 30), "10:15:30") }
  test("MonthDay") { assertContract("MonthDay", MonthDay.of(8, 3), "--08-03") }
  test("OffsetDateTime") {
    assertContract("OffsetDateTime", OffsetDateTime.parse("2026-08-03T10:15:30+02:00"), "2026-08-03T10:15:30+02:00")
  }
  test("OffsetTime") { assertContract("OffsetTime", OffsetTime.parse("10:15:30+02:00"), "10:15:30+02:00") }
  test("Period") { assertContract("Period", Period.ofDays(3), "P3D") }
  test("Year") { assertContract("Year", Year.of(2026), "2026") }
  test("YearMonth") { assertContract("YearMonth", YearMonth.of(2026, 8), "2026-08") }
  test("ZonedDateTime") {
    assertContract("ZonedDateTime", ZonedDateTime.parse("2026-08-03T10:15:30+02:00"), "2026-08-03T10:15:30+02:00")
  }
}
