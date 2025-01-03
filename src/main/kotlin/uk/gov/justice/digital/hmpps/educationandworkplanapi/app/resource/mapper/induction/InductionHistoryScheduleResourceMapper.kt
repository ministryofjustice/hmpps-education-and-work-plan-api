package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleResponse
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule as InductionScheduleCalculationRuleResponse

@Component
class InductionHistoryScheduleResourceMapper(
  private val instantMapper: InstantMapper,
  private val inductionScheduleResourceMapper: InductionScheduleResourceMapper,
  private val userService: ManageUserService,

) {
  fun toInductionResponse(inductionScheduleHistory: InductionScheduleHistory, induction: Induction?): InductionScheduleResponse {
    with(inductionScheduleHistory) {
      val isCompleted = scheduleStatus == InductionScheduleStatus.COMPLETED
      return InductionScheduleResponse(
        reference = reference,
        prisonNumber = prisonNumber,
        deadlineDate = deadlineDate,
        scheduleCalculationRule = toInductionScheduleCalculationRule(scheduleCalculationRule),
        scheduleStatus = inductionScheduleResourceMapper.toInductionScheduleStatus(scheduleStatus),
        createdBy = createdBy!!,
        createdByDisplayName = userService.getUserDetails(createdBy!!).name,
        createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
        updatedBy = lastUpdatedBy!!,
        updatedByDisplayName = userService.getUserDetails(lastUpdatedBy!!).name,
        updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
        inductionPerformedBy = if (isCompleted) induction?.let { userService.getUserDetails(it.lastUpdatedBy!!).name } else null,
        inductionPerformedAt = if (isCompleted) induction?.lastUpdatedAt?.atZone(ZoneId.systemDefault())?.toLocalDate() else null,
        version = version,
      )
    }
  }

  fun toInductionScheduleCalculationRule(inductionScheduleCalculationRule: InductionScheduleCalculationRule): InductionScheduleCalculationRuleResponse =
    when (inductionScheduleCalculationRule) {
      InductionScheduleCalculationRule.NEW_PRISON_ADMISSION -> InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION
      InductionScheduleCalculationRule.EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE -> InductionScheduleCalculationRuleResponse.EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE
      InductionScheduleCalculationRule.EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE -> InductionScheduleCalculationRuleResponse.EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE
      InductionScheduleCalculationRule.EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE -> InductionScheduleCalculationRuleResponse.EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE
      InductionScheduleCalculationRule.EXISTING_PRISONER_INDETERMINATE_SENTENCE -> InductionScheduleCalculationRuleResponse.EXISTING_PRISONER_INDETERMINATE_SENTENCE
      InductionScheduleCalculationRule.EXISTING_PRISONER_ON_REMAND -> InductionScheduleCalculationRuleResponse.EXISTING_PRISONER_ON_REMAND
      InductionScheduleCalculationRule.EXISTING_PRISONER_UN_SENTENCED -> InductionScheduleCalculationRuleResponse.EXISTING_PRISONER_UN_SENTENCED
    }
}
