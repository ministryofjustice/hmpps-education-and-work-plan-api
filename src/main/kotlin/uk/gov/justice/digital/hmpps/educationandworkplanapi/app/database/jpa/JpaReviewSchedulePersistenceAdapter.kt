package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewSchedulePersistenceAdapter
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
      if (reviewScheduleRepository.findByPrisonNumber(prisonNumber) != null) {
        throw ReviewScheduleAlreadyExistsException(prisonNumber)
      }

      val persistedEntity = reviewScheduleRepository.saveAndFlush(
        reviewScheduleEntityMapper.fromDomainToEntity(this),
      )
      reviewScheduleEntityMapper.fromEntityToDomain(persistedEntity)
    }

  @Transactional
  override fun updateReviewSchedule(updateReviewScheduleDto: UpdateReviewScheduleDto): ReviewSchedule? {
    val reviewScheduleEntity = reviewScheduleRepository.findByReference(updateReviewScheduleDto.reference)

    return if (reviewScheduleEntity != null) {
      reviewScheduleEntityMapper.updateExistingEntityFromDto(reviewScheduleEntity, updateReviewScheduleDto)
      val persistedEntity = reviewScheduleRepository.saveAndFlush(reviewScheduleEntity)
      return reviewScheduleEntityMapper.fromEntityToDomain(persistedEntity)
    } else {
      null
    }
  }

  @Transactional(readOnly = true)
  override fun getReviewSchedule(prisonNumber: String): ReviewSchedule? =
    reviewScheduleRepository.findByPrisonNumber(prisonNumber)?.let {
      reviewScheduleEntityMapper.fromEntityToDomain(it)
    }
}
