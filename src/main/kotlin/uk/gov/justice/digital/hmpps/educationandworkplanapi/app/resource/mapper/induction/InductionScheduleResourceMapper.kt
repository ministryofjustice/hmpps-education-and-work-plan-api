package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleResponse
import java.time.LocalDate
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule as InductionScheduleCalculationRuleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusResponse

@Component
class InductionScheduleResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,

) {
  fun toInductionResponse(inductionSchedule: InductionSchedule, induction: Induction?): InductionScheduleResponse {
    with(inductionSchedule) {
      var inductionPerformedBy: String? = null
      var inductionPerformedByRole: String? = null
      var inductionPerformedAt: LocalDate? = null
      var inductionPerformedAtPrison: String? = null

      // Only set inductionPerformed* fields if the status is COMPLETED
      if (scheduleStatus == InductionScheduleStatus.COMPLETED && induction != null) {
        inductionPerformedBy = induction.conductedBy ?: userService.getUserDetails(induction.lastUpdatedBy!!).name
        inductionPerformedByRole = induction.conductedByRole
        inductionPerformedAt = induction.createdAt
          ?.atZone(ZoneId.systemDefault())
          ?.toLocalDate()
        inductionPerformedAtPrison = induction.createdAtPrison
      }

      return InductionScheduleResponse(
        reference = reference,
        prisonNumber = prisonNumber,
        deadlineDate = deadlineDate,
        scheduleCalculationRule = toInductionScheduleCalculationRule(scheduleCalculationRule),
        scheduleStatus = toInductionScheduleStatus(scheduleStatus),
        createdBy = createdBy,
        createdByDisplayName = userService.getUserDetails(createdBy).name,
        createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
        updatedBy = lastUpdatedBy,
        createdAtPrison = createdAtPrison,
        updatedByDisplayName = userService.getUserDetails(lastUpdatedBy).name,
        updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
        inductionPerformedBy = inductionPerformedBy,
        inductionPerformedAt = inductionPerformedAt,
        inductionPerformedByRole = inductionPerformedByRole,
        inductionPerformedAtPrison = inductionPerformedAtPrison,
        updatedAtPrison = lastUpdatedAtPrison,
      )
    }
  }

  fun toInductionScheduleCalculationRule(inductionScheduleCalculationRule: InductionScheduleCalculationRule): InductionScheduleCalculationRuleResponse = when (inductionScheduleCalculationRule) {
    InductionScheduleCalculationRule.NEW_PRISON_ADMISSION -> InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION
    InductionScheduleCalculationRule.EXISTING_PRISONER -> InductionScheduleCalculationRuleResponse.EXISTING_PRISONER
  }

  fun toInductionScheduleStatus(inductionScheduleStatus: InductionScheduleStatus): InductionScheduleStatusResponse = when (inductionScheduleStatus) {
    InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS -> InductionScheduleStatusResponse.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
    InductionScheduleStatus.SCHEDULED -> InductionScheduleStatusResponse.SCHEDULED
    InductionScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY -> InductionScheduleStatusResponse.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
    InductionScheduleStatus.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES -> InductionScheduleStatusResponse.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
    InductionScheduleStatus.EXEMPT_PRISONER_FAILED_TO_ENGAGE -> InductionScheduleStatusResponse.EXEMPT_PRISONER_FAILED_TO_ENGAGE
    InductionScheduleStatus.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED -> InductionScheduleStatusResponse.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED
    InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES -> InductionScheduleStatusResponse.EXEMPT_PRISONER_SAFETY_ISSUES
    InductionScheduleStatus.EXEMPT_PRISON_REGIME_CIRCUMSTANCES -> InductionScheduleStatusResponse.EXEMPT_PRISON_REGIME_CIRCUMSTANCES
    InductionScheduleStatus.EXEMPT_PRISON_STAFF_REDEPLOYMENT -> InductionScheduleStatusResponse.EXEMPT_PRISON_STAFF_REDEPLOYMENT
    InductionScheduleStatus.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE -> InductionScheduleStatusResponse.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE
    InductionScheduleStatus.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF -> InductionScheduleStatusResponse.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF
    InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> InductionScheduleStatusResponse.EXEMPT_SYSTEM_TECHNICAL_ISSUE
    InductionScheduleStatus.EXEMPT_PRISONER_TRANSFER -> InductionScheduleStatusResponse.EXEMPT_PRISONER_TRANSFER
    InductionScheduleStatus.EXEMPT_PRISONER_RELEASE -> InductionScheduleStatusResponse.EXEMPT_PRISONER_RELEASE
    InductionScheduleStatus.EXEMPT_PRISONER_DEATH -> InductionScheduleStatusResponse.EXEMPT_PRISONER_DEATH
    InductionScheduleStatus.EXEMPT_PRISONER_MERGE -> InductionScheduleStatusResponse.EXEMPT_PRISONER_MERGE
    InductionScheduleStatus.EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS -> InductionScheduleStatusResponse.EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS
    InductionScheduleStatus.EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE -> InductionScheduleStatusResponse.EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE
    InductionScheduleStatus.COMPLETED -> InductionScheduleStatusResponse.COMPLETED
  }
}
