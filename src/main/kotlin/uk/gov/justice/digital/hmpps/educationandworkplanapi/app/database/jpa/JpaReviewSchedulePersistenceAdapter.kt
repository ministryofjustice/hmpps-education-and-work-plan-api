package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import jakarta.persistence.NonUniqueResultException
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ActiveReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review.ReviewScheduleEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository

private val log = KotlinLogging.logger {}

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
    try {
      reviewScheduleRepository.findActiveReviewSchedule(prisonNumber)
        ?.let { reviewScheduleEntityMapper.fromEntityToDomain(it) }
    } catch (e: NonUniqueResultException) {
      log.error { "Prisoner $prisonNumber has more than one active ReviewSchedule which is not supported. Please investigate the ReviewSchedule data for prisoner $prisonNumber" }
      throw IllegalStateException("A prisoner cannot have more than one active ReviewSchedule. Please investigate the ReviewSchedule data for prisoner $prisonNumber")
    }

  @Transactional(readOnly = true)
  override fun getLatestReviewSchedule(prisonNumber: String): ReviewSchedule? =
    reviewScheduleRepository.findFirstByPrisonNumberOrderByUpdatedAtDesc(prisonNumber)
      ?.let { reviewScheduleEntityMapper.fromEntityToDomain(it) }
}
