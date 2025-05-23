package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidCreateCompletedReviewDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidCreateActionPlanReviewRequest
import java.time.LocalDate

class CreateActionPlanReviewRequestMapperTest {
  private val mapper = CreateActionPlanReviewRequestMapper()

  @Test
  fun `should map from model to domain`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerReleaseDate = LocalDate.now().plusYears(5)
    val prisonerSentenceType = SentenceType.SENTENCED
    val isIndeterminate = true
    val isRecall = true

    val expected = aValidCreateCompletedReviewDto(
      prisonNumber = prisonNumber,
      prisonerReleaseDate = prisonerReleaseDate,
      prisonerSentenceType = prisonerSentenceType,
      prisonerHasIndeterminateFlag = isIndeterminate,
      prisonerHasRecallFlag = isRecall,
    )

    val createActionPlanReviewRequest = aValidCreateActionPlanReviewRequest()

    // When
    val actual = mapper.fromModelToDomain(prisonNumber, prisonerReleaseDate, prisonerSentenceType, isIndeterminate, isRecall, createActionPlanReviewRequest)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
