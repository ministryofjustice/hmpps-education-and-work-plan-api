package uk.gov.justice.digital.hmpps.educationandworkplanapi

import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

fun OffsetDateTime.isBeforeRounded(dateTime: OffsetDateTime): Boolean {
  val thisRounded = this.truncatedTo(ChronoUnit.MILLIS)
  val dateTimeRounded = dateTime.truncatedTo(ChronoUnit.MILLIS)
  return thisRounded.isBefore(dateTimeRounded)
}

fun Instant.isBeforeRounded(dateTime: Instant): Boolean {
  val thisRounded = this.truncatedTo(ChronoUnit.MILLIS)
  val dateTimeRounded = dateTime.truncatedTo(ChronoUnit.MILLIS)
  return thisRounded.isBefore(dateTimeRounded)
}
