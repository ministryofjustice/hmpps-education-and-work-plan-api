package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionScheduleStatusDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleHistoryEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.InductionScheduleEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleHistoryRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleRepository
import java.time.LocalDate

@Component
class JpaInductionSchedulePersistenceAdapter(
  private val inductionScheduleRepository: InductionScheduleRepository,
  private val inductionScheduleHistoryRepository: InductionScheduleHistoryRepository,
  private val inductionScheduleEntityMapper: InductionScheduleEntityMapper,
) : InductionSchedulePersistenceAdapter {
  @Transactional
  override fun createInductionSchedule(createInductionScheduleDto: CreateInductionScheduleDto): InductionSchedule =
    inductionScheduleRepository.saveAndFlush(
      inductionScheduleEntityMapper.fromCreateDtoToEntity(
        createInductionScheduleDto,
      ),
    ).let {
      saveInductionScheduleHistory(it)
      inductionScheduleEntityMapper.fromEntityToDomain(it)
    }

  @Transactional(readOnly = true)
  override fun getInductionSchedule(prisonNumber: String): InductionSchedule? =
    inductionScheduleRepository.findByPrisonNumber(prisonNumber)?.let {
      inductionScheduleEntityMapper.fromEntityToDomain(it)
    }

  override fun getActiveInductionSchedule(prisonNumber: String): InductionSchedule? =
    inductionScheduleRepository.findByPrisonNumberAndScheduleStatusIn(
      prisonNumber = prisonNumber,
      scheduleStatuses = InductionScheduleStatus.ACTIVE_STATUSES,
    )
      ?.let { inductionScheduleEntityMapper.fromEntityToDomain(it) }

  override fun getInCompleteInductionSchedules(prisonerNumbers: List<String>): List<InductionSchedule> {
    return inductionScheduleRepository.findAllByPrisonNumberInAndScheduleStatusNot(prisonerNumbers)
      .map { inductionScheduleEntityMapper.fromEntityToDomain(it) }
  }

  @Transactional
  override fun updateSchedule(
    prisonNumber: String,
    calculationRule: InductionScheduleCalculationRule,
    deadlineDate: LocalDate,
  ): InductionSchedule {
    // Retrieve and validate the existing induction schedule exists.
    val existingSchedule = inductionScheduleRepository.findByPrisonNumber(prisonNumber)
      ?: throw InductionScheduleNotFoundException(prisonNumber)

    // Update the induction schedule with the new values.
    val updatedSchedule = existingSchedule.copy(
      deadlineDate = deadlineDate,
      scheduleCalculationRule = inductionScheduleEntityMapper.toInductionScheduleCalculationRule(calculationRule),
    )

    // Save the updated schedule and return the mapped domain object.
    return inductionScheduleRepository.saveAndFlush(updatedSchedule).let {
      saveInductionScheduleHistory(it)
      inductionScheduleEntityMapper.fromEntityToDomain(it)
    }
  }

  override fun getInductionScheduleHistory(prisonNumber: String): List<InductionScheduleHistory> {
    return inductionScheduleHistoryRepository.findAllByPrisonNumber(prisonNumber)
      .map { inductionScheduleEntityMapper.fromScheduleHistoryEntityToDomain(it) }
  }

  override fun updateInductionScheduleStatus(updateInductionScheduleStatusDto: UpdateInductionScheduleStatusDto): InductionSchedule {
    val inductionScheduleEntity =
      inductionScheduleRepository.findByPrisonNumber(updateInductionScheduleStatusDto.prisonNumber)
        ?: throw InductionScheduleNotFoundException(updateInductionScheduleStatusDto.prisonNumber)

    // Update the schedule status and optionally the latest review date
    inductionScheduleEntity.apply {
      scheduleStatus =
        inductionScheduleEntityMapper.toInductionScheduleStatus(updateInductionScheduleStatusDto.scheduleStatus)
      exemptionReason = updateInductionScheduleStatusDto.exemptionReason
      updateInductionScheduleStatusDto.latestDeadlineDate?.let { deadlineDate = it }
      updatedAtPrison = updateInductionScheduleStatusDto.updatedAtPrison
    }

    return inductionScheduleRepository.saveAndFlush(inductionScheduleEntity).let {
      saveInductionScheduleHistory(it)
      inductionScheduleEntityMapper.fromEntityToDomain(it)
    }
  }

  private fun saveInductionScheduleHistory(inductionScheduleEntity: InductionScheduleEntity) {
    with(inductionScheduleEntity) {
      val historyEntry = InductionScheduleHistoryEntity(
        version = inductionScheduleHistoryRepository.findMaxVersionByInductionScheduleReference(reference)
          ?.plus(1) ?: 1,
        reference = reference,
        prisonNumber = prisonNumber,
        updatedAt = updatedAt!!,
        updatedAtPrison = updatedAtPrison,
        createdAt = createdAt!!,
        createdAtPrison = createdAtPrison,
        updatedBy = updatedBy!!,
        createdBy = createdBy!!,
        scheduleStatus = scheduleStatus,
        exemptionReason = exemptionReason,
        scheduleCalculationRule = scheduleCalculationRule,
        deadlineDate = deadlineDate,
      )
      inductionScheduleHistoryRepository.save(historyEntry)
    }
  }
}
