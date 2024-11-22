package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch

import java.time.LocalDate

data class PagedPrisonerResponse(
  val last: Boolean,
  val content: List<Prisoner>,
)

data class Prisoner(
  val prisonerNumber: String,
  val legalStatus: LegalStatus,
  val releaseDate: LocalDate?,
  val prisonId: String?,
)

enum class LegalStatus {
  RECALL,
  DEAD,
  INDETERMINATE_SENTENCE,
  SENTENCED,
  CONVICTED_UNSENTENCED,
  CIVIL_PRISONER,
  IMMIGRATION_DETAINEE,
  REMAND,
  UNKNOWN,
  OTHER,
}
