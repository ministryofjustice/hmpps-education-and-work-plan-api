package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule as InductionScheduleCalculationRuleDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus as InductionScheduleStatusDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleCalculationRule as InductionScheduleCalculationRuleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus as InductionScheduleStatusEntity

@Component
class InductionScheduleEntityMapper {

  fun fromEntityToDomain(entity: InductionScheduleEntity): InductionSchedule =
    with(entity) {
      InductionSchedule(
        reference = reference,
        prisonNumber = prisonNumber,
        deadlineDate = deadlineDate,
        scheduleCalculationRule = toInductionScheduleCalculationRule(scheduleCalculationRule),
        scheduleStatus = toInductionScheduleStatus(scheduleStatus),
      )
    }

  fun fromCreateDtoToEntity(createInductionScheduleDto: CreateInductionScheduleDto): InductionScheduleEntity =
    with(createInductionScheduleDto) {
      InductionScheduleEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        deadlineDate = deadlineDate,
        scheduleCalculationRule = toInductionScheduleCalculationRule(scheduleCalculationRule),
        scheduleStatus = InductionScheduleStatusEntity.SCHEDULED,
      )
    }

  fun toInductionScheduleCalculationRule(inductionScheduleCalculationRule: InductionScheduleCalculationRuleEntity): InductionScheduleCalculationRuleDomain =
    when (inductionScheduleCalculationRule) {
      InductionScheduleCalculationRuleEntity.NEW_PRISON_ADMISSION -> InductionScheduleCalculationRuleDomain.NEW_PRISON_ADMISSION
      InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE -> InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE
      InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE -> InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE
      InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE -> InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE
      InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_INDETERMINATE_SENTENCE -> InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_INDETERMINATE_SENTENCE
      InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_ON_REMAND -> InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_ON_REMAND
      InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_UN_SENTENCED -> InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_UN_SENTENCED
    }

  fun toInductionScheduleCalculationRule(inductionScheduleCalculationRule: InductionScheduleCalculationRuleDomain): InductionScheduleCalculationRuleEntity =
    when (inductionScheduleCalculationRule) {
      InductionScheduleCalculationRuleDomain.NEW_PRISON_ADMISSION -> InductionScheduleCalculationRuleEntity.NEW_PRISON_ADMISSION
      InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE -> InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE
      InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE -> InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE
      InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE -> InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE
      InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_INDETERMINATE_SENTENCE -> InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_INDETERMINATE_SENTENCE
      InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_ON_REMAND -> InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_ON_REMAND
      InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_UN_SENTENCED -> InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_UN_SENTENCED
    }

  private fun toInductionScheduleStatus(inductionScheduleStatus: InductionScheduleStatusEntity): InductionScheduleStatusDomain =
    when (inductionScheduleStatus) {
      InductionScheduleStatusEntity.SCHEDULED -> InductionScheduleStatusDomain.SCHEDULED
      InductionScheduleStatusEntity.COMPLETE -> InductionScheduleStatusDomain.COMPLETE
      InductionScheduleStatusEntity.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY -> InductionScheduleStatusDomain.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
      InductionScheduleStatusEntity.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES -> InductionScheduleStatusDomain.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
      InductionScheduleStatusEntity.EXEMPT_PRISONER_FAILED_TO_ENGAGE -> InductionScheduleStatusDomain.EXEMPT_PRISONER_FAILED_TO_ENGAGE
      InductionScheduleStatusEntity.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED -> InductionScheduleStatusDomain.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED
      InductionScheduleStatusEntity.EXEMPT_PRISONER_SAFETY_ISSUES -> InductionScheduleStatusDomain.EXEMPT_PRISONER_SAFETY_ISSUES
      InductionScheduleStatusEntity.EXEMPT_PRISON_REGIME_CIRCUMSTANCES -> InductionScheduleStatusDomain.EXEMPT_PRISON_REGIME_CIRCUMSTANCES
      InductionScheduleStatusEntity.EXEMPT_PRISON_STAFF_REDEPLOYMENT -> InductionScheduleStatusDomain.EXEMPT_PRISON_STAFF_REDEPLOYMENT
      InductionScheduleStatusEntity.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE -> InductionScheduleStatusDomain.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE
      InductionScheduleStatusEntity.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF -> InductionScheduleStatusDomain.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF
      InductionScheduleStatusEntity.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> InductionScheduleStatusDomain.EXEMPT_SYSTEM_TECHNICAL_ISSUE
      InductionScheduleStatusEntity.EXEMPT_PRISONER_TRANSFER -> InductionScheduleStatusDomain.EXEMPT_PRISONER_TRANSFER
      InductionScheduleStatusEntity.EXEMPT_PRISONER_RELEASE -> InductionScheduleStatusDomain.EXEMPT_PRISONER_RELEASE
      InductionScheduleStatusEntity.EXEMPT_PRISONER_DEATH -> InductionScheduleStatusDomain.EXEMPT_PRISONER_DEATH
    }

  private fun toInductionScheduleStatus(inductionScheduleStatus: InductionScheduleStatusDomain): InductionScheduleStatusEntity =
    when (inductionScheduleStatus) {
      InductionScheduleStatusDomain.SCHEDULED -> InductionScheduleStatusEntity.SCHEDULED
      InductionScheduleStatusDomain.COMPLETE -> InductionScheduleStatusEntity.COMPLETE
      InductionScheduleStatusDomain.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY -> InductionScheduleStatusEntity.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
      InductionScheduleStatusDomain.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES -> InductionScheduleStatusEntity.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
      InductionScheduleStatusDomain.EXEMPT_PRISONER_FAILED_TO_ENGAGE -> InductionScheduleStatusEntity.EXEMPT_PRISONER_FAILED_TO_ENGAGE
      InductionScheduleStatusDomain.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED -> InductionScheduleStatusEntity.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED
      InductionScheduleStatusDomain.EXEMPT_PRISONER_SAFETY_ISSUES -> InductionScheduleStatusEntity.EXEMPT_PRISONER_SAFETY_ISSUES
      InductionScheduleStatusDomain.EXEMPT_PRISON_REGIME_CIRCUMSTANCES -> InductionScheduleStatusEntity.EXEMPT_PRISON_REGIME_CIRCUMSTANCES
      InductionScheduleStatusDomain.EXEMPT_PRISON_STAFF_REDEPLOYMENT -> InductionScheduleStatusEntity.EXEMPT_PRISON_STAFF_REDEPLOYMENT
      InductionScheduleStatusDomain.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE -> InductionScheduleStatusEntity.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE
      InductionScheduleStatusDomain.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF -> InductionScheduleStatusEntity.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF
      InductionScheduleStatusDomain.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> InductionScheduleStatusEntity.EXEMPT_SYSTEM_TECHNICAL_ISSUE
      InductionScheduleStatusDomain.EXEMPT_PRISONER_TRANSFER -> InductionScheduleStatusEntity.EXEMPT_PRISONER_TRANSFER
      InductionScheduleStatusDomain.EXEMPT_PRISONER_RELEASE -> InductionScheduleStatusEntity.EXEMPT_PRISONER_RELEASE
      InductionScheduleStatusDomain.EXEMPT_PRISONER_DEATH -> InductionScheduleStatusEntity.EXEMPT_PRISONER_DEATH
    }
}
