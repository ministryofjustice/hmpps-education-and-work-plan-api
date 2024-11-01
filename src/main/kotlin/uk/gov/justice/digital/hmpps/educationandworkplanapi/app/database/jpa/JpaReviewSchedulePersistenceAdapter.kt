package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review.ReviewScheduleEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository

@Component
class JpaReviewSchedulePersistenceAdapter(
  private val reviewScheduleRepository: ReviewScheduleRepository,
  private val reviewScheduleEntityMapper: ReviewScheduleEntityMapper,
) : ReviewSchedulePersistenceAdapter {

  @Transactional(readOnly = true)
  override fun getReviewSchedule(prisonNumber: String): ReviewSchedule? =
    reviewScheduleRepository.findByPrisonNumber(prisonNumber)?.let {
      reviewScheduleEntityMapper.fromEntityToDomain(it)
    }
}
