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
      rescheduleInductionSchedule(this, newInductionDeadlineDate, prisonId)
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
        exemptInductionSchedule(inductionSchedule, newStatus, exemptionReason, prisonId)
      }

      else -> {
        val adjustedInductionDueDate =
          inductionScheduleDateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)
        rescheduleInductionSchedule(inductionSchedule, adjustedInductionDueDate, prisonId)
      }
    }
  }

  private fun exemptInductionSchedule(
    inductionSchedule: InductionSchedule,
    newStatus: InductionScheduleStatus,
    exemptionReason: String?,
    prisonId: String,
  ): InductionSchedule =
    with(inductionSchedule) {
      inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
        UpdateInductionScheduleStatusDto(
          reference = reference,
          scheduleStatus = newStatus,
          exemptionReason = exemptionReason,
          prisonNumber = prisonNumber,
          updatedAtPrison = prisonId,
        ),
      ).also {
        performFollowOnEvents(
          updatedInductionSchedule = it,
          oldStatus = scheduleStatus,
          oldDeadlineDate = deadlineDate,
        )
      }
    }

  private fun rescheduleInductionSchedule(
    inductionSchedule: InductionSchedule,
    newInductionDueDate: LocalDate,
    prisonId: String,
  ): InductionSchedule =
    with(inductionSchedule) {
      inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
        UpdateInductionScheduleStatusDto(
          reference = reference,
          scheduleStatus = SCHEDULED,
          latestDeadlineDate = newInductionDueDate,
          prisonNumber = prisonNumber,
          updatedAtPrison = prisonId,
        ),
      ).also {
        performFollowOnEvents(
          updatedInductionSchedule = it,
          oldStatus = scheduleStatus,
          oldDeadlineDate = deadlineDate,
        )
      }
    }

  private fun updateInductionScheduleFollowingSystemTechnicalIssue(
    inductionSchedule: InductionSchedule,
    exemptionReason: String?,
    prisonId: String,
  ): InductionSchedule {
    // Update the induction schedule status to EXEMPT_SYSTEM_TECHNICAL_ISSUE
    val updatedInductionScheduleFirst = inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
      UpdateInductionScheduleStatusDto(
        reference = inductionSchedule.reference,
        scheduleStatus = InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
        exemptionReason = exemptionReason,
        prisonNumber = inductionSchedule.prisonNumber,
        updatedAtPrison = prisonId,
      ),
    )
    performFollowOnEvents(
      updatedInductionSchedule = updatedInductionScheduleFirst,
      oldStatus = inductionSchedule.scheduleStatus,
      oldDeadlineDate = inductionSchedule.deadlineDate,
    )

    // Then update the induction schedule to be SCHEDULED with an adjusted induction date
    val adjustedInductionDate =
      inductionScheduleDateCalculationService.calculateAdjustedInductionDueDate(updatedInductionScheduleFirst)
    return inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
      UpdateInductionScheduleStatusDto(
        reference = inductionSchedule.reference,
        scheduleStatus = SCHEDULED,
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
        updatedAt = updatedInductionSchedule.lastUpdatedAt,
        updatedBy = updatedInductionSchedule.lastUpdatedBy,
      ),
    )
  }

  /**
   * Updates the prisoner's Induction Schedule by setting its status to EXEMPT_PRISONER_DEATH.
   */
  fun exemptActiveInductionScheduleStatusDueToPrisonerDeath(prisonNumber: String, prisonId: String) {
    exemptActiveInductionScheduleStatus(
      prisonNumber = prisonNumber,
      prisonId = prisonId,
      newStatus = InductionScheduleStatus.EXEMPT_PRISONER_DEATH,
    )
  }

  /**
   * Updates the prisoner's Induction Schedule by setting its status to EXEMPT_PRISONER_RELEASE.
   */
  fun exemptActiveInductionScheduleStatusDueToPrisonerRelease(prisonNumber: String, prisonId: String) {
    exemptActiveInductionScheduleStatus(
      prisonNumber = prisonNumber,
      prisonId = prisonId,
      newStatus = InductionScheduleStatus.EXEMPT_PRISONER_RELEASE,
    )
  }

  /**
   * Shared logic for updating the Induction Schedule status with the given status.
   */
  private fun exemptActiveInductionScheduleStatus(
    prisonNumber: String,
    prisonId: String,
    newStatus: InductionScheduleStatus,
  ) {
    val inductionSchedule = inductionSchedulePersistenceAdapter.getActiveInductionSchedule(prisonNumber)
      ?: throw InductionScheduleNotFoundException(prisonNumber)

    val oldStatus = inductionSchedule.scheduleStatus
    inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
      UpdateInductionScheduleStatusDto(
        reference = inductionSchedule.reference,
        scheduleStatus = newStatus,
        exemptionReason = inductionSchedule.exemptionReason,
        prisonNumber = prisonNumber,
        updatedAtPrison = prisonId,
      ),
    ).also {
      performFollowOnEvents(
        updatedInductionSchedule = it,
        oldStatus = oldStatus,
        oldDeadlineDate = inductionSchedule.deadlineDate,
      )
    }
  }
}
