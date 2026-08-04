package com.evolution.playjson.jsoniter

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsError, JsNumber, JsString, JsSuccess, Reads}

import java.time._

/**
  * Contract tests for the java.time formats of [[PlayJsonJsoniter]]: every format writes a value
  * that it can read back, and a value it cannot read is reported against the name of the type it
  * was asked to read.
  */
class JavaTimeFormatsSpec extends AnyFunSuite with Matchers {

  import PlayJsonJsoniter._

  private def assertRoundTrip[A](value: A)(implicit format: Format[A]): Unit = {
    format.reads(format.writes(value)) shouldEqual JsSuccess(value)
    ()
  }

  private def assertReadErrorNames[A](name: String)(implicit reads: Reads[A]): Unit = {
    reads.reads(JsString("not a " + name)) shouldEqual JsError(name)
    reads.reads(JsNumber(1)) shouldEqual JsError(name)
    ()
  }

  test("Duration round-trips") { assertRoundTrip(Duration.ofSeconds(90)) }
  test("Instant round-trips") { assertRoundTrip(Instant.parse("2026-08-03T10:15:30Z")) }
  test("LocalDate round-trips") { assertRoundTrip(LocalDate.of(2026, 8, 3)) }
  test("LocalDateTime round-trips") { assertRoundTrip(LocalDateTime.of(2026, 8, 3, 10, 15, 30)) }
  test("LocalTime round-trips") { assertRoundTrip(LocalTime.of(10, 15, 30)) }
  test("MonthDay round-trips") { assertRoundTrip(MonthDay.of(8, 3)) }
  test("OffsetDateTime round-trips") { assertRoundTrip(OffsetDateTime.parse("2026-08-03T10:15:30+02:00")) }
  test("OffsetTime round-trips") { assertRoundTrip(OffsetTime.parse("10:15:30+02:00")) }
  test("Period round-trips") { assertRoundTrip(Period.ofDays(3)) }
  test("YearMonth round-trips") { assertRoundTrip(YearMonth.of(2026, 8)) }
  test("ZonedDateTime round-trips") { assertRoundTrip(ZonedDateTime.parse("2026-08-03T10:15:30+02:00")) }

  test("Duration reports its own type name when it cannot read") { assertReadErrorNames[Duration]("Duration") }
  test("Instant reports its own type name when it cannot read") { assertReadErrorNames[Instant]("Instant") }
  test("LocalDate reports its own type name when it cannot read") { assertReadErrorNames[LocalDate]("LocalDate") }
  test("LocalDateTime reports its own type name when it cannot read") {
    assertReadErrorNames[LocalDateTime]("LocalDateTime")
  }
  test("LocalTime reports its own type name when it cannot read") { assertReadErrorNames[LocalTime]("LocalTime") }
  test("MonthDay reports its own type name when it cannot read") { assertReadErrorNames[MonthDay]("MonthDay") }
  test("OffsetDateTime reports its own type name when it cannot read") {
    assertReadErrorNames[OffsetDateTime]("OffsetDateTime")
  }
  test("OffsetTime reports its own type name when it cannot read") { assertReadErrorNames[OffsetTime]("OffsetTime") }
  test("Period reports its own type name when it cannot read") { assertReadErrorNames[Period]("Period") }
  test("Year reports its own type name when it cannot read") { assertReadErrorNames[Year]("Year") }
  test("YearMonth reports its own type name when it cannot read") { assertReadErrorNames[YearMonth]("YearMonth") }
  test("ZonedDateTime reports its own type name when it cannot read") {
    assertReadErrorNames[ZonedDateTime]("ZonedDateTime")
  }
}
