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
  val lastName: String,
  val firstName: String,
  val dateOfBirth: LocalDate,
  val cellLocation: String?,
  @field:JsonProperty(value = "nonDtoReleaseDateType")
  val releaseType: String?, // TODO this needs to be checked
  val receptionDate: LocalDate?,
  val sentenceStartDate: LocalDate?,
  val allConvictedOffences: List<ConvictedOffence>? = emptyList<ConvictedOffence>(),
  val inOutStatus: String?,
)

data class ConvictedOffence(
  val statuteCode: String,
  val offenceCode: String,
  val offenceDescription: String,
  val offenceDate: LocalDate?,
  val latestBooking: Boolean,
  val sentenceStartDate: LocalDate?,
  val primarySentence: Boolean,
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
