package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ActiveReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review.ReviewScheduleEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository

@Component
class JpaReviewSchedulePersistenceAdapter(
  private val reviewScheduleRepository: ReviewScheduleRepository,
  private val reviewScheduleEntityMapper: ReviewScheduleEntityMapper,
) : ReviewSchedulePersistenceAdapter {

  @Transactional
  override fun createReviewSchedule(createReviewScheduleDto: CreateReviewScheduleDto): ReviewSchedule =
    with(createReviewScheduleDto) {
      if (getActiveReviewSchedule(prisonNumber) != null) {
        throw ActiveReviewScheduleAlreadyExistsException(prisonNumber)
      }

      val persistedEntity = reviewScheduleRepository.saveAndFlush(
        reviewScheduleEntityMapper.fromDomainToEntity(this),
      )
      reviewScheduleEntityMapper.fromEntityToDomain(persistedEntity)
    }

  @Transactional
  override fun updateReviewSchedule(updateReviewScheduleDto: UpdateReviewScheduleDto): ReviewSchedule? {
    val reviewScheduleEntity = reviewScheduleRepository.findByReference(updateReviewScheduleDto.reference)

    return reviewScheduleEntity?.let {
      reviewScheduleEntityMapper.updateExistingEntityFromDto(it, updateReviewScheduleDto)
      reviewScheduleEntityMapper.fromEntityToDomain(reviewScheduleRepository.saveAndFlush(it))
    }
  }

  @Transactional(readOnly = true)
  override fun getActiveReviewSchedule(prisonNumber: String): ReviewSchedule? =
    with(
      reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
        .filter { it.scheduleStatus != ReviewScheduleStatus.COMPLETED },
    ) {
      if (size > 1) {
        throw IllegalStateException("A prisoner cannot have more than one active ReviewSchedule. Please investigate the ReviewSchedule data for prisoner $prisonNumber")
      }
      firstOrNull()
        ?.let { reviewScheduleEntityMapper.fromEntityToDomain(it) }
    }

  @Transactional(readOnly = true)
  override fun getLatestReviewSchedule(prisonNumber: String): ReviewSchedule? =
    reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
      .maxByOrNull { it.updatedAt!! }
      ?.let { reviewScheduleEntityMapper.fromEntityToDomain(it) }
}
