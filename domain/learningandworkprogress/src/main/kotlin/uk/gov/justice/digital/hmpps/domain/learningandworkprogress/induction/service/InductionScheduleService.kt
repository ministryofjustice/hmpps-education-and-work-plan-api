package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.SCHEDULED
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
  fun createInductionSchedule(
    prisonNumber: String,
    prisonerAdmissionDate: LocalDate,
    prisonId: String,
    newAdmission: Boolean = true,
  ): InductionSchedule {
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
      prisonId = prisonId,
      newAdmission = newAdmission,
    )
    return inductionSchedulePersistenceAdapter.createInductionSchedule(createInductionScheduleDto)
      .also {
        inductionScheduleEventService.inductionScheduleCreated(it)
      }
  }

  /**
   * Re-schedule the prisoner's Induction Schedule.
   *
   * Unless the Induction Schedule is already COMPLETED or PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS the
   * status is set to SCHEDULED and a new deadline date is set.
   * The new deadline date is set as it were a new Induction Schedule via the [InductionScheduleDateCalculationService]
   *
   * Throws [InductionScheduleNotFoundException] ff the prisoner does not have an [InductionSchedule].
   */
  fun reschedulePrisonersInductionSchedule(
    prisonNumber: String,
    prisonerAdmissionDate: LocalDate,
    prisonId: String,
  ): InductionSchedule {
    val inductionSchedule = inductionSchedulePersistenceAdapter.getInductionSchedule(prisonNumber)
      ?: throw InductionScheduleNotFoundException(prisonNumber)

    return with(inductionSchedule) {
      // Validate the status transition
      inductionScheduleStatusTransitionValidator.validate(prisonNumber, scheduleStatus, SCHEDULED)

      // A rescheduled Induction Schedule must have its due date set as if it were a new Induction Schedule based on the prisoner's admission date
      val newInductionDeadlineDate = inductionScheduleDateCalculationService.determineCreateInductionScheduleDto(
        prisonNumber = prisonNumber,
        admissionDate = prisonerAdmissionDate,
        prisonId = prisonId,
      ).deadlineDate

      updateInductionSchedule(
        inductionSchedule = this,
        newStatus = SCHEDULED,
        prisonId = prisonId,
        adjustedInductionDate = newInductionDeadlineDate,
      )
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
    prisonId: String,
  ): InductionSchedule {
    val inductionSchedule = inductionSchedulePersistenceAdapter.getInductionSchedule(prisonNumber)
      ?: throw InductionScheduleNotFoundException(prisonNumber)

    // Validate the status transition
    inductionScheduleStatusTransitionValidator.validate(prisonNumber, inductionSchedule.scheduleStatus, newStatus)

    return when {
      newStatus == InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> {
        updateInductionScheduleFollowingSystemTechnicalIssue(inductionSchedule, exemptionReason, prisonId)
      }

      newStatus.isExemptionOrExclusion() -> {
        updateInductionSchedule(
          inductionSchedule = inductionSchedule,
          newStatus = newStatus,
          prisonId = prisonId,
          exemptionReason = exemptionReason,
        )
      }

      else -> {
        val adjustedInductionDueDate =
          inductionScheduleDateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)
        updateInductionSchedule(
          inductionSchedule = inductionSchedule,
          newStatus = SCHEDULED,
          prisonId = prisonId,
          adjustedInductionDate = adjustedInductionDueDate,
        )
      }
    }
  }

  private fun updateInductionScheduleFollowingSystemTechnicalIssue(
    inductionSchedule: InductionSchedule,
    exemptionReason: String?,
    prisonId: String,
  ): InductionSchedule {
    // Step 1: Mark as EXEMPT_SYSTEM_TECHNICAL_ISSUE
    val updatedSchedule = updateInductionSchedule(
      inductionSchedule = inductionSchedule,
      newStatus = InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
      exemptionReason = exemptionReason,
      prisonId = prisonId,
    )

    // Step 2: Adjust induction date and reschedule
    val adjustedInductionDate = inductionScheduleDateCalculationService
      .calculateAdjustedInductionDueDate(updatedSchedule)
    return updateInductionSchedule(
      inductionSchedule = updatedSchedule,
      newStatus = SCHEDULED,
      adjustedInductionDate = adjustedInductionDate,
      prisonId = prisonId,
    )
  }

  fun exemptAndReScheduleActiveInductionScheduleDueToPrisonerTransfer(
    prisonTransferredTo: String,
    prisonNumber: String,
  ): InductionSchedule {
    val inductionSchedule = inductionSchedulePersistenceAdapter.getActiveInductionSchedule(prisonNumber)
      ?: throw InductionScheduleNotFoundException(prisonNumber)
    // Step 1: Mark as EXEMPT_PRISONER_TRANSFER
    val updatedSchedule = updateInductionSchedule(
      inductionSchedule = inductionSchedule,
      newStatus = InductionScheduleStatus.EXEMPT_PRISONER_TRANSFER,
      prisonId = inductionSchedule.lastUpdatedAtPrison,
    )

    // Step 2: Adjust induction date and reschedule
    val adjustedInductionDate = inductionScheduleDateCalculationService
      .calculateAdjustedInductionDueDate(updatedSchedule)
    return updateInductionSchedule(
      inductionSchedule = updatedSchedule,
      newStatus = SCHEDULED,
      adjustedInductionDate = adjustedInductionDate,
      prisonId = prisonTransferredTo,
    )
  }

  /**
   * Updates the prisoner's Induction Schedule by setting its status to EXEMPT_PRISONER_DEATH.
   */
  fun exemptActiveInductionScheduleStatusDueToPrisonerDeath(prisonNumber: String, prisonId: String) {
    val inductionSchedule = inductionSchedulePersistenceAdapter.getActiveInductionSchedule(prisonNumber)
      ?: throw InductionScheduleNotFoundException(prisonNumber)

    updateInductionSchedule(
      inductionSchedule = inductionSchedule,
      newStatus = InductionScheduleStatus.EXEMPT_PRISONER_DEATH,
      prisonId = prisonId,
    )
  }

  /**
   * Updates the prisoner's Induction Schedule by setting its status to EXEMPT_PRISONER_RELEASE.
   */
  fun exemptActiveInductionScheduleStatusDueToPrisonerRelease(prisonNumber: String, prisonId: String) {
    val inductionSchedule = inductionSchedulePersistenceAdapter.getActiveInductionSchedule(prisonNumber)
      ?: throw InductionScheduleNotFoundException(prisonNumber)

    updateInductionSchedule(
      inductionSchedule = inductionSchedule,
      newStatus = InductionScheduleStatus.EXEMPT_PRISONER_RELEASE,
      prisonId = prisonId,
    )
  }

  private fun updateInductionSchedule(
    inductionSchedule: InductionSchedule,
    newStatus: InductionScheduleStatus,
    exemptionReason: String? = null,
    adjustedInductionDate: LocalDate = inductionSchedule.deadlineDate,
    prisonId: String,
  ): InductionSchedule {
    val updatedSchedule = inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
      UpdateInductionScheduleStatusDto(
        reference = inductionSchedule.reference,
        scheduleStatus = newStatus,
        exemptionReason = exemptionReason,
        latestDeadlineDate = adjustedInductionDate,
        prisonNumber = inductionSchedule.prisonNumber,
        updatedAtPrison = prisonId,
      ),
    ).also {
      performFollowOnEvents(
        updatedInductionSchedule = it,
        oldStatus = inductionSchedule.scheduleStatus,
        oldDeadlineDate = inductionSchedule.deadlineDate,
      )
    }
    return updatedSchedule
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
        updatedAtPrison = updatedInductionSchedule.lastUpdatedAtPrison,
        oldStatus = oldStatus,
        newStatus = updatedInductionSchedule.scheduleStatus,
        exemptionReason = updatedInductionSchedule.exemptionReason,
        newDeadlineDate = updatedInductionSchedule.deadlineDate,
        oldDeadlineDate = oldDeadlineDate,
        updatedAt = updatedInductionSchedule.lastUpdatedAt,
        updatedBy = updatedInductionSchedule.lastUpdatedBy,
      ),
    )
  }
}
