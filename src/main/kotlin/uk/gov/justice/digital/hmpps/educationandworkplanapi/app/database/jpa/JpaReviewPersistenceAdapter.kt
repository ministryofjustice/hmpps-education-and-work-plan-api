package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateCompletedReviewDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review.ReviewEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.NoteRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewRepository
import java.util.UUID

@Component
class JpaReviewPersistenceAdapter(
  private val reviewRepository: ReviewRepository,
  private val reviewEntityMapper: ReviewEntityMapper,
  private val noteRepository: NoteRepository,
) : ReviewPersistenceAdapter {

  @Transactional(readOnly = true)
  override fun getCompletedReviews(prisonNumber: String): List<CompletedReview> =
    reviewRepository.getAllByPrisonNumber(prisonNumber).map {
      val reviewNoteEntity = noteRepository.findAllByEntityReferenceAndEntityType(
        entityReference = it.reference,
        entityType = EntityType.REVIEW,
      ).first()
      reviewEntityMapper.fromEntityToDomain(it, reviewNoteEntity)
    }

  @Transactional
  override fun createCompletedReview(createCompletedReviewDto: CreateCompletedReviewDto, reviewSchedule: ReviewSchedule): CompletedReview =
    with(createCompletedReviewDto) {
      val completedReviewEntity = reviewRepository.saveAndFlush(
        reviewEntityMapper.fromDomainToEntity(this, reviewSchedule),
      )
      val noteEntity = noteRepository.saveAndFlush(
        NoteEntity(
          reference = UUID.randomUUID(),
          prisonNumber = prisonNumber,
          content = note,
          noteType = NoteType.REVIEW,
          entityType = EntityType.REVIEW,
          entityReference = completedReviewEntity.reference,
          createdAtPrison = prisonId,
          updatedAtPrison = prisonId,
        ),
      )

      reviewEntityMapper.fromEntityToDomain(completedReviewEntity, noteEntity)
    }

  @Transactional
  override fun setPreRelease(reference: UUID) {
    reviewRepository.setPreRelease(reference)
  }
}
