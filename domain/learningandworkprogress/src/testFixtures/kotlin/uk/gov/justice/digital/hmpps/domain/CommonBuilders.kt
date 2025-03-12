package uk.gov.justice.digital.hmpps.domain

import java.util.UUID

/**
 * Builder functions for common data items that are not aligned to a specific domain, REST model or JPA entity; such as
 * prison numbers, times and dates etc.
 */

fun aValidPrisonNumber() = "A1234BC"

fun randomValidPrisonNumber(): String {
  val letters = ('A'..'Z')
  val numbers = ('0'..'9')

  val firstLetter = letters.random()
  val secondLetter = letters.random()
  val thirdLetter = letters.random()

  val numberPart = numbers.random().toString().padStart(4, '0')

  return "$firstLetter$numberPart$secondLetter$thirdLetter"
}

fun aValidReference() = UUID.randomUUID()
