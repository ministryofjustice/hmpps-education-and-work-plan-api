package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidCreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidUpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.aValidReviewScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.aValidUnPersistedReviewScheduleEntity
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule as ReviewScheduleCalculationRuleDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus as ReviewScheduleStatusDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleCalculationRule as ReviewScheduleCalculationRuleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus as ReviewScheduleStatusEntity

class ReviewScheduleEntityMapperTest {
  private val mapper = ReviewScheduleEntityMapper()

  @Test
  fun `should map review schedule entity to review schedule domain`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val reference = UUID.randomUUID()
    val createdAt = Instant.now()
    val updatedAt = Instant.now()

    val entity = aValidReviewScheduleEntity(
      prisonNumber = prisonNumber,
      reference = reference,
      createdAt = createdAt,
      updatedAt = updatedAt,
    )
    val expected = aValidReviewSchedule(
      prisonNumber = prisonNumber,
      reference = reference,
      createdAt = createdAt,
      lastUpdatedAt = updatedAt,
    )

    // When
    val actual = mapper.fromEntityToDomain(entity)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map create review schedule domain DTO to review schedule entity`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val earliestReviewDate = LocalDate.now().plusMonths(1)
    val latestReviewDate = LocalDate.now().plusMonths(3)
    val createReviewScheduleDto = aValidCreateReviewScheduleDto(
      prisonNumber = prisonNumber,
      scheduleCalculationRule = ReviewScheduleCalculationRuleDomain.MORE_THAN_60_MONTHS_TO_SERVE,
      reviewScheduleWindow = ReviewScheduleWindow(earliestReviewDate, latestReviewDate),
    )

    val expected = aValidUnPersistedReviewScheduleEntity(
      prisonNumber = prisonNumber,
      scheduleCalculationRule = ReviewScheduleCalculationRuleEntity.MORE_THAN_60_MONTHS_TO_SERVE,
      earliestReviewDate = earliestReviewDate,
      latestReviewDate = latestReviewDate,
    )

    // When
    val actual = mapper.fromDomainToEntity(createReviewScheduleDto)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("reference")
      .isEqualTo(expected)
  }

  @Test
  fun `should update review schedule entity from update review schedule domain DTO`() {
    // Given
    val reviewScheduleEntity = aValidReviewScheduleEntity(
      earliestReviewDate = LocalDate.now().minusMonths(1),
      latestReviewDate = LocalDate.now().plusDays(1),
      scheduleCalculationRule = ReviewScheduleCalculationRuleEntity.PRISONER_TRANSFER,
      scheduleStatus = ReviewScheduleStatusEntity.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES,
      updatedAtPrison = "BXI",
    )

    val updatedEarliestReviewDate = LocalDate.now().plusMonths(10)
    val updatedLatestReviewDate = LocalDate.now().plusMonths(12)
    val updateReviewScheduleDto = aValidUpdateReviewScheduleDto(
      reviewScheduleWindow = ReviewScheduleWindow(updatedEarliestReviewDate, updatedLatestReviewDate),
      scheduleCalculationRule = ReviewScheduleCalculationRuleDomain.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
      scheduleStatus = ReviewScheduleStatusDomain.SCHEDULED,
      prisonId = "LFI",
    )

    // When
    mapper.updateExistingEntityFromDto(reviewScheduleEntity, updateReviewScheduleDto)

    // Then
    assertThat(reviewScheduleEntity.earliestReviewDate).isEqualTo(updatedEarliestReviewDate)
    assertThat(reviewScheduleEntity.latestReviewDate).isEqualTo(updatedLatestReviewDate)
    assertThat(reviewScheduleEntity.scheduleStatus).isEqualTo(ReviewScheduleStatusEntity.SCHEDULED)
    assertThat(reviewScheduleEntity.scheduleCalculationRule).isEqualTo(ReviewScheduleCalculationRuleEntity.BETWEEN_12_AND_60_MONTHS_TO_SERVE)
    assertThat(reviewScheduleEntity.updatedAtPrison).isEqualTo("LFI")
  }
}
