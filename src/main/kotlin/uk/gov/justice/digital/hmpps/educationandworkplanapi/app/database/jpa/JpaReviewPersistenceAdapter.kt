package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review.ReviewEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.NoteRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewRepository

@Component
class JpaReviewPersistenceAdapter(
  private val reviewRepository: ReviewRepository,
  private val reviewEntityMapper: ReviewEntityMapper,
  private val noteRepository: NoteRepository,
) : ReviewPersistenceAdapter {

  override fun getCompletedReviews(prisonNumber: String): List<CompletedReview> =
    reviewRepository.getAllByPrisonNumber(prisonNumber).map {
      val reviewNoteEntity = noteRepository.findAllByEntityReferenceAndEntityType(
        entityReference = it.reference,
        entityType = EntityType.REVIEW,
      ).first()
      reviewEntityMapper.fromEntityToDomain(it, reviewNoteEntity)
    }
}
