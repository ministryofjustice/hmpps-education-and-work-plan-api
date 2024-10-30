package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.aValidReviewScheduleEntity
import java.time.Instant
import java.util.UUID

class ReviewScheduleEntityMapperTest {
  private val mapper = ReviewScheduleEntityMapper()

  @Test
  fun `should map review schedule entity to review schedule domain`() {
    // Given
    val reference = UUID.randomUUID()
    val createdAt = Instant.now()
    val updatedAt = Instant.now()

    val entity = aValidReviewScheduleEntity(
      reference = reference,
      createdAt = createdAt,
      updatedAt = updatedAt,
    )
    val expected = aValidReviewSchedule(
      reference = reference,
      createdAt = createdAt,
      lastUpdatedAt = updatedAt,
    )

    // When
    val actual = mapper.fromEntityToDomain(entity)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
