package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class PagedPrisonerResponse(
  val last: Boolean,
  val content: List<Prisoner>,
)

data class Prisoner(
  val prisonerNumber: String,
  val legalStatus: LegalStatus = LegalStatus.OTHER,
  val releaseDate: LocalDate?,
  val prisonId: String?,
  @field:JsonProperty(value = "indeterminateSentence", defaultValue = "false")
  val isIndeterminateSentence: Boolean,
  @field:JsonProperty(value = "recall", defaultValue = "false")
  val isRecall: Boolean,
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
