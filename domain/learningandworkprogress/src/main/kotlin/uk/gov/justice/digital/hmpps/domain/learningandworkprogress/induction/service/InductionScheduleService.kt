package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.UpdatedInductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionScheduleStatusDto
import java.time.LocalDate

private const val EXEMPTION_ADDITIONAL_DAYS = 5L
private const val EXCLUSION_ADDITIONAL_DAYS = 10L
private const val SYSTEM_OUTAGE_ADDITIONAL_DAYS = 5L
class InductionScheduleService(
  private val inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter,
  private val inductionScheduleEventService: InductionScheduleEventService,
) {

  private val inductionScheduleStatusTransitionValidator = InductionScheduleStatusTransitionValidator()

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
    val newInductionDate = calculateNewInductionDate(inductionSchedule)
    val updatedInductionSchedule = inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
      UpdateInductionScheduleStatusDto(
        reference = inductionSchedule.reference,
        scheduleStatus = InductionScheduleStatus.SCHEDULED,
        latestDeadlineDate = newInductionDate,
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

    // Then update the induction schedule to be SCHEDULED with a new induction date
    val newInductionDate = calculateNewInductionDate(inductionSchedule, SYSTEM_OUTAGE_ADDITIONAL_DAYS)
    val updatedInductionScheduleSecond = inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(
      UpdateInductionScheduleStatusDto(
        reference = inductionSchedule.reference,
        scheduleStatus = InductionScheduleStatus.SCHEDULED,
        latestDeadlineDate = newInductionDate,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedInductionSchedule = updatedInductionScheduleSecond,
      oldStatus = inductionSchedule.scheduleStatus,
      oldDeadlineDate = inductionSchedule.deadlineDate,
    )
  }

  private fun calculateNewInductionDate(
    inductionSchedule: InductionSchedule,
    additionalDays: Long = getExtensionDays(inductionSchedule.scheduleStatus),
  ): LocalDate? {
    val todayPlusAdditionalDays = LocalDate.now().plusDays(additionalDays)
    return maxOf(todayPlusAdditionalDays, inductionSchedule.deadlineDate)
  }

  private fun getExtensionDays(status: InductionScheduleStatus): Long {
    return when {
      status.isExclusion -> EXCLUSION_ADDITIONAL_DAYS
      status.isExemption -> EXEMPTION_ADDITIONAL_DAYS
      else -> 0 // Default case, if no condition matches
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
        updatedAt = updatedInductionSchedule.lastUpdatedAt!!,
        updatedBy = updatedInductionSchedule.lastUpdatedBy!!,
      ),
    )
  }
}
