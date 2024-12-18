package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateInitialReviewScheduleDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.LegalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner

@Component
class CreateInitialReviewScheduleMapper {

  fun fromPrisonerToDomain(prisoner: Prisoner, isTransfer: Boolean = false, isReadmission: Boolean = false): CreateInitialReviewScheduleDto =
    with(prisoner) {
      CreateInitialReviewScheduleDto(
        prisonNumber = prisonerNumber,
        prisonId = prisonId ?: "N/A",
        prisonerReleaseDate = releaseDate,
        prisonerSentenceType = toSentenceType(legalStatus),
        prisonerHasIndeterminateFlag = isIndeterminateSentence,
        prisonerHasRecallFlag = isRecall,
        isTransfer = isTransfer,
        isReadmission = isReadmission,
      )
    }

  private fun toSentenceType(legalStatus: LegalStatus): SentenceType =
    when (legalStatus) {
      LegalStatus.RECALL -> SentenceType.RECALL
      LegalStatus.DEAD -> SentenceType.DEAD
      LegalStatus.INDETERMINATE_SENTENCE -> SentenceType.INDETERMINATE_SENTENCE
      LegalStatus.SENTENCED -> SentenceType.SENTENCED
      LegalStatus.CONVICTED_UNSENTENCED -> SentenceType.CONVICTED_UNSENTENCED
      LegalStatus.CIVIL_PRISONER -> SentenceType.CIVIL_PRISONER
      LegalStatus.IMMIGRATION_DETAINEE -> SentenceType.IMMIGRATION_DETAINEE
      LegalStatus.REMAND -> SentenceType.REMAND
      LegalStatus.UNKNOWN -> SentenceType.UNKNOWN
      LegalStatus.OTHER -> SentenceType.OTHER
    }
}
