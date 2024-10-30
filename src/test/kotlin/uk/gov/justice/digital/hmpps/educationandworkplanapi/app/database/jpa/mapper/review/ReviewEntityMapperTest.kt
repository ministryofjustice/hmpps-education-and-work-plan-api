package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewConductedBy
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidCompletedReview
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.aValidNoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.aValidReviewEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.note.NoteMapper
import java.time.Instant
import java.util.UUID

class ReviewEntityMapperTest {
  private val mapper = ReviewEntityMapper()

  @Test
  fun `should map review entity to completed review given conducted by fields are populated`() {
    // Given
    val reference = UUID.randomUUID()
    val createdAt = Instant.now()

    val reviewEntity = aValidReviewEntity(
      reference = reference,
      conductedBy = "Barnie Jones",
      conductedByRole = "Peer mentor",
      createdAt = createdAt,
    )
    val reviewNoteEntity = aValidNoteEntity(
      entityReference = reference,
      entityType = EntityType.REVIEW,
    )

    val expectedNote = NoteMapper.fromEntityToDomain(reviewNoteEntity)
    val expected = aValidCompletedReview(
      reference = reference,
      note = expectedNote,
      conductedBy = ReviewConductedBy(name = "Barnie Jones", role = "Peer mentor"),
      createdAt = createdAt,
    )

    // When
    val actual = mapper.fromEntityToDomain(reviewEntity, reviewNoteEntity)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map review entity to completed review given conducted by fields are not populated`() {
    // Given
    val reference = UUID.randomUUID()
    val createdAt = Instant.now()

    val reviewEntity = aValidReviewEntity(
      reference = reference,
      conductedBy = null,
      conductedByRole = null,
      createdAt = createdAt,
    )
    val reviewNoteEntity = aValidNoteEntity(
      entityReference = reference,
      entityType = EntityType.REVIEW,
    )

    val expectedNote = NoteMapper.fromEntityToDomain(reviewNoteEntity)
    val expected = aValidCompletedReview(
      reference = reference,
      note = expectedNote,
      conductedBy = null,
      createdAt = createdAt,
    )

    // When
    val actual = mapper.fromEntityToDomain(reviewEntity, reviewNoteEntity)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
