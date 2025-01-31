package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.annotation.JsonValue

/**
 * An enumeration of HMPPS Domain Events that this service consumes and processes.
 */
enum class EventType(@JsonValue val eventType: String) {
  PRISONER_RECEIVED_INTO_PRISON("prison-offender-events.prisoner.received"),
  PRISONER_RELEASED_FROM_PRISON("prison-offender-events.prisoner.released"),
  PRISONER_MERGED("prison-offender-events.prisoner.merged"),
}
