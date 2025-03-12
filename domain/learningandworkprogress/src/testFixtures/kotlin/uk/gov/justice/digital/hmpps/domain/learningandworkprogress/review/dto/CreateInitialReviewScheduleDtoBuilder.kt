package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.LocalDate

fun aValidCreateInitialReviewScheduleDto(
  prisonNumber: String = randomValidPrisonNumber(),
  prisonId: String = "BXI",
  prisonerReleaseDate: LocalDate? = LocalDate.now().plusYears(1),
  prisonerSentenceType: SentenceType = SentenceType.SENTENCED,
  prisonerHasIndeterminateFlag: Boolean = false,
  prisonerHasRecallFlag: Boolean = false,
  isReadmission: Boolean = false,
  isTransfer: Boolean = false,
): CreateInitialReviewScheduleDto =
  CreateInitialReviewScheduleDto(
    prisonNumber = prisonNumber,
    prisonId = prisonId,
    prisonerReleaseDate = prisonerReleaseDate,
    prisonerSentenceType = prisonerSentenceType,
    prisonerHasIndeterminateFlag = prisonerHasIndeterminateFlag,
    prisonerHasRecallFlag = prisonerHasRecallFlag,
    isReadmission = isReadmission,
    isTransfer = isTransfer,
  )
