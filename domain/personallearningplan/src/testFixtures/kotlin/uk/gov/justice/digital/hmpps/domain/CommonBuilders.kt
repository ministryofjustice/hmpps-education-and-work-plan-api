package uk.gov.justice.digital.hmpps.domain

import java.util.UUID

/**
 * Builder functions for common data items that are not aligned to a specific domain, REST model or JPA entity; such as
 * prison numbers, times and dates etc.
 */

fun aValidPrisonNumber() = "A1234BC"

fun anotherValidPrisonNumber() = "B5678CD"

fun aValidReference() = UUID.randomUUID()
