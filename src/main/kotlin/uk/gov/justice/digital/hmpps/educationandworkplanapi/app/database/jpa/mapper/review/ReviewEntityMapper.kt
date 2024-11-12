package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewConductedBy
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateCompletedReviewDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.note.NoteMapper
import java.time.LocalDate
import java.util.UUID

@Component
class ReviewEntityMapper {

  fun fromEntityToDomain(reviewEntity: ReviewEntity, reviewNoteEntity: NoteEntity): CompletedReview =
    with(reviewEntity) {
      CompletedReview(
        reference = reference,
        prisonNumber = prisonNumber,
        deadlineDate = deadlineDate,
        completedDate = completedDate,
        note = NoteMapper.fromEntityToDomain(reviewNoteEntity),
        createdBy = createdBy!!,
        createdAt = createdAt!!,
        createdAtPrison = createdAtPrison,
        conductedBy = toReviewConductedBy(this),
      )
    }

  fun fromDomainToEntity(createCompletedReviewDto: CreateCompletedReviewDto, deadlineDate: LocalDate): ReviewEntity =
    with(createCompletedReviewDto) {
      ReviewEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        deadlineDate = deadlineDate,
        completedDate = conductedAt,
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
        conductedBy = conductedBy,
        conductedByRole = conductedByRole,
      )
    }

  private fun toReviewConductedBy(reviewEntity: ReviewEntity): ReviewConductedBy? =
    reviewEntity.takeIf { reviewEntity.conductedBy != null && reviewEntity.conductedByRole != null }
      ?.let { ReviewConductedBy(name = it.conductedBy!!, role = it.conductedByRole!!) }
}
