package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.UpdatedInductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionScheduleStatusDto
import java.time.LocalDate

private val log = KotlinLogging.logger {}

class InductionScheduleService(
  private val inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter,
  private val inductionScheduleEventService: InductionScheduleEventService,
  private val inductionScheduleDateCalculationService: InductionScheduleDateCalculationService,
) {

  private val inductionScheduleStatusTransitionValidator = InductionScheduleStatusTransitionValidator()

  /**
   * Creates the prisoner's [InductionSchedule].
   * A prisoner can only have 1 Induction Schedule. Throws [InductionScheduleAlreadyExistsException] if the prisoner
   * already has an Induction Schedule regardless of status (scheduled, completed, exempt etc.)
   */
  fun createInductionSchedule(prisonNumber: String, prisonerAdmissionDate: LocalDate): InductionSchedule {
    // Check for an existing Induction Schedule
    val inductionSchedule = runCatching {
      getInductionScheduleForPrisoner(prisonNumber)
    }.getOrNull()
    if (inductionSchedule != null) {
      throw InductionScheduleAlreadyExistsException(inductionSchedule)
    }

    log.info { "Creating Induction Schedule for prisoner [$prisonNumber]" }
    val createInductionScheduleDto = inductionScheduleDateCalculationService.determineCreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      admissionDate = prisonerAdmissionDate,
    )
    return inductionSchedulePersistenceAdapter.createInductionSchedule(createInductionScheduleDto)
      .also {
        inductionScheduleEventService.inductionScheduleCreated(it)
      }
  }

  fun getInductionScheduleForPrisoner(prisonNumber: String): InductionSchedule =
    inductionSchedulePersistenceAdapter.getInductionSchedule(prisonNumber)
      ?: throw InductionScheduleNotFoundException(prisonNumber)

  fun getInductionScheduleHistoryForPrisoner(prisonNumber: String): List<InductionScheduleHistory> {
    val responses = inductionSchedulePersistenceAdapter.getInductionScheduleHistory(prisonNumber)

    return responses.sortedWith(
      compareByDescending { it.version },
    )
  }

  fun updateLatestInductionScheduleStatus(
    prisonNumber: String,
    newStatus: InductionScheduleStatus,
    exemptionReason: String?,
  ) {
    val inductionSchedule = inductionSchedulePersistenceAdapter.getInductionSchedule(prisonNumber)
      ?: throw InductionScheduleNotFoundException(prisonNumber)

    // Validate the status transition
    inductionScheduleStatusTransitionValidator.validate(prisonNumber, inductionSchedule.scheduleStatus, newStatus)

    when {
      newStatus == InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> {
        updateInductionScheduleFollowingSystemTechnicalIssue(inductionSchedule, exemptionReason, prisonNumber)
      }

      newStatus.isExemptionOrExclusion() -> {
        updateExemptStatus(inductionSchedule, newStatus, exemptionReason, prisonNumber)
      }

      else -> {
        updateScheduledStatus(inductionSchedule, prisonNumber)
      }
    }
  }

  private fun updateExemptStatus(
    inductionSchedule: InductionSchedule,
    newStatus: InductionScheduleStatus,
    exemptionReason: String?,
    prisonNumber: String,
  ) {
    val updatedInductionSchedule = inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
      UpdateInductionScheduleStatusDto(
        reference = inductionSchedule.reference,
        scheduleStatus = newStatus,
        exemptionReason = exemptionReason,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedInductionSchedule = updatedInductionSchedule,
      oldStatus = inductionSchedule.scheduleStatus,
      oldDeadlineDate = inductionSchedule.deadlineDate,
    )
  }

  private fun updateScheduledStatus(
    inductionSchedule: InductionSchedule,
    prisonNumber: String,
  ) {
    val adjustedInductionDate = inductionScheduleDateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)
    val updatedInductionSchedule = inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
      UpdateInductionScheduleStatusDto(
        reference = inductionSchedule.reference,
        scheduleStatus = InductionScheduleStatus.SCHEDULED,
        latestDeadlineDate = adjustedInductionDate,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedInductionSchedule = updatedInductionSchedule,
      oldStatus = inductionSchedule.scheduleStatus,
      oldDeadlineDate = inductionSchedule.deadlineDate,
    )
  }

  private fun updateInductionScheduleFollowingSystemTechnicalIssue(
    inductionSchedule: InductionSchedule,
    exemptionReason: String?,
    prisonNumber: String,
  ) {
    // Update the induction schedule status to EXEMPT_SYSTEM_TECHNICAL_ISSUE
    val updatedInductionScheduleFirst = inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
      UpdateInductionScheduleStatusDto(
        reference = inductionSchedule.reference,
        scheduleStatus = InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
        exemptionReason = exemptionReason,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedInductionSchedule = updatedInductionScheduleFirst,
      oldStatus = inductionSchedule.scheduleStatus,
      oldDeadlineDate = inductionSchedule.deadlineDate,
    )

    // Then update the induction schedule to be SCHEDULED with an adjusted induction date
    val adjustedInductionDate = inductionScheduleDateCalculationService.calculateAdjustedInductionDueDate(updatedInductionScheduleFirst)
    val updatedInductionScheduleSecond = inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
      UpdateInductionScheduleStatusDto(
        reference = inductionSchedule.reference,
        scheduleStatus = InductionScheduleStatus.SCHEDULED,
        latestDeadlineDate = adjustedInductionDate,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedInductionSchedule = updatedInductionScheduleSecond,
      oldStatus = inductionSchedule.scheduleStatus,
      oldDeadlineDate = inductionSchedule.deadlineDate,
    )
  }

  private fun performFollowOnEvents(
    oldStatus: InductionScheduleStatus,
    oldDeadlineDate: LocalDate,
    updatedInductionSchedule: InductionSchedule,
  ) {
    inductionScheduleEventService.inductionScheduleStatusUpdated(
      UpdatedInductionScheduleStatus(
        reference = updatedInductionSchedule.reference,
        prisonNumber = updatedInductionSchedule.prisonNumber,
        oldStatus = oldStatus,
        newStatus = updatedInductionSchedule.scheduleStatus,
        exemptionReason = updatedInductionSchedule.exemptionReason,
        newDeadlineDate = updatedInductionSchedule.deadlineDate,
        oldDeadlineDate = oldDeadlineDate,
        updatedAt = updatedInductionSchedule.lastUpdatedAt!!,
        updatedBy = updatedInductionSchedule.lastUpdatedBy!!,
      ),
    )
  }
}
