package uk.gov.justice.digital.hmpps.domain

import java.util.UUID

/**
 * Builder functions for common data items that are not aligned to a specific domain, REST model or JPA entity; such as
 * prison numbers, times and dates etc.
 */

fun randomValidPrisonNumber(): String {
  fun randomLetter() = ('A'..'Z').random()
  fun randomNumbers(count: Int) = (1..count).map { ('0'..'9').random() }.joinToString("")
  return "${randomLetter()}${randomNumbers(4)}${randomLetter()}${randomLetter()}"
}
fun aValidReference(): UUID = UUID.randomUUID()
