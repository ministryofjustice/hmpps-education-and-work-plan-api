package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateCompletedReviewDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanReviewRequest
import java.time.LocalDate

@Component
class CreateActionPlanReviewRequestMapper {

  fun fromModelToDomain(
    prisonNumber: String,
    releaseDate: LocalDate?,
    sentenceType: SentenceType,
    isIndeterminateSentence: Boolean,
    isRecall: Boolean,
    request: CreateActionPlanReviewRequest,
  ): CreateCompletedReviewDto =
    with(request) {
      CreateCompletedReviewDto(
        prisonNumber = prisonNumber,
        prisonId = prisonId,
        note = note,
        conductedAt = conductedAt,
        conductedBy = conductedBy,
        conductedByRole = conductedByRole,
        prisonerReleaseDate = releaseDate,
        prisonerSentenceType = sentenceType,
        prisonerHasIndeterminateFlag = isIndeterminateSentence,
        prisonerHasRecallFlag = isRecall,
      )
    }
}
