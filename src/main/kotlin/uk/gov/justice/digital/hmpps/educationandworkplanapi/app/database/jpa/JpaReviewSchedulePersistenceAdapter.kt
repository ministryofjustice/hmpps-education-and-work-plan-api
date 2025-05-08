package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import jakarta.persistence.NonUniqueResultException
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ActiveReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleStatusDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleHistoryEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.Companion.STATUSES_FOR_ACTIVE_REVIEWS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review.ReviewScheduleEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleHistoryRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository

private val log = KotlinLogging.logger {}

@Component
class JpaReviewSchedulePersistenceAdapter(
  private val reviewScheduleRepository: ReviewScheduleRepository,
  private val reviewScheduleHistoryRepository: ReviewScheduleHistoryRepository,
  private val reviewScheduleEntityMapper: ReviewScheduleEntityMapper,
) : ReviewSchedulePersistenceAdapter {

  @Transactional
  override fun createReviewSchedule(createReviewScheduleDto: CreateReviewScheduleDto): ReviewSchedule = with(createReviewScheduleDto) {
    if (getActiveReviewSchedule(prisonNumber) != null) {
      throw ActiveReviewScheduleAlreadyExistsException(prisonNumber)
    }

    reviewScheduleRepository.saveAndFlush(
      reviewScheduleEntityMapper.fromDomainToEntity(this),
    ).let {
      saveReviewScheduleHistory(it)
      reviewScheduleEntityMapper.fromEntityToDomain(it)
    }
  }

  @Transactional
  override fun updateReviewSchedule(updateReviewScheduleDto: UpdateReviewScheduleDto): ReviewSchedule? {
    val reviewScheduleEntity = reviewScheduleRepository.findByReference(updateReviewScheduleDto.reference)

    return reviewScheduleEntity?.let {
      reviewScheduleEntityMapper.updateExistingEntityFromDto(it, updateReviewScheduleDto)
      reviewScheduleRepository.saveAndFlush(it).let { saved ->
        saveReviewScheduleHistory(saved)
        reviewScheduleEntityMapper.fromEntityToDomain(saved)
      }
    }
  }

  @Transactional(readOnly = true)
  override fun getActiveReviewSchedule(prisonNumber: String): ReviewSchedule? = try {
    reviewScheduleRepository.findByPrisonNumberAndScheduleStatusIn(prisonNumber = prisonNumber, scheduleStatuses = STATUSES_FOR_ACTIVE_REVIEWS)
      ?.let { reviewScheduleEntityMapper.fromEntityToDomain(it) }
  } catch (e: NonUniqueResultException) {
    log.error { "Prisoner $prisonNumber has more than one active ReviewSchedule which is not supported. Please investigate the ReviewSchedule data for prisoner $prisonNumber" }
    throw IllegalStateException("A prisoner cannot have more than one active ReviewSchedule. Please investigate the ReviewSchedule data for prisoner $prisonNumber")
  }

  @Transactional(readOnly = true)
  override fun getLatestReviewSchedule(prisonNumber: String): ReviewSchedule? = reviewScheduleRepository.findFirstByPrisonNumberOrderByUpdatedAtDesc(prisonNumber)
    ?.let { reviewScheduleEntityMapper.fromEntityToDomain(it) }

  override fun updateReviewScheduleStatus(updateReviewScheduleStatusDto: UpdateReviewScheduleStatusDto): ReviewSchedule {
    val reviewScheduleEntity = reviewScheduleRepository.findByReference(updateReviewScheduleStatusDto.reference)
      ?: throw ReviewScheduleNotFoundException(updateReviewScheduleStatusDto.prisonNumber)

    // Update the schedule status and optionally the earliest and latest review dates
    reviewScheduleEntity.apply {
      scheduleStatus = reviewScheduleEntityMapper.toReviewScheduleStatus(updateReviewScheduleStatusDto.scheduleStatus)
      exemptionReason = updateReviewScheduleStatusDto.exemptionReason
      updatedAtPrison = updateReviewScheduleStatusDto.prisonId
      updateReviewScheduleStatusDto.earliestReviewDate?.let { earliestReviewDate = it }
      updateReviewScheduleStatusDto.latestReviewDate?.let { latestReviewDate = it }
    }

    return reviewScheduleRepository.saveAndFlush(reviewScheduleEntity).let {
      saveReviewScheduleHistory(it)
      reviewScheduleEntityMapper.fromEntityToDomain(it)
    }
  }

  override fun getInCompleteReviewSchedules(prisonerNumbers: List<String>): List<ReviewSchedule> = reviewScheduleRepository.findAllByPrisonNumberInAndScheduleStatusNot(prisonerNumbers)
    .map { reviewScheduleEntityMapper.fromEntityToDomain(it) }

  private fun saveReviewScheduleHistory(reviewScheduleEntity: ReviewScheduleEntity) {
    with(reviewScheduleEntity) {
      val historyEntry = ReviewScheduleHistoryEntity(
        version = reviewScheduleHistoryRepository.findMaxVersionByReviewScheduleReference(reference)
          ?.plus(1) ?: 1,
        reference = reference,
        prisonNumber = prisonNumber,
        createdAtPrison = createdAtPrison,
        updatedAtPrison = updatedAtPrison,
        updatedAt = updatedAt!!,
        createdAt = createdAt!!,
        updatedBy = updatedBy!!,
        createdBy = createdBy!!,
        scheduleStatus = scheduleStatus,
        exemptionReason = exemptionReason,
        earliestReviewDate = earliestReviewDate,
        latestReviewDate = latestReviewDate,
        scheduleCalculationRule = scheduleCalculationRule,
      )
      reviewScheduleHistoryRepository.save(historyEntry)
    }
  }

  override fun getReviewScheduleHistory(prisonNumber: String): List<ReviewScheduleHistory> = reviewScheduleHistoryRepository.findAllByPrisonNumber(prisonNumber)
    .map { reviewScheduleEntityMapper.fromScheduleHistoryEntityToDomain(it) }
}
