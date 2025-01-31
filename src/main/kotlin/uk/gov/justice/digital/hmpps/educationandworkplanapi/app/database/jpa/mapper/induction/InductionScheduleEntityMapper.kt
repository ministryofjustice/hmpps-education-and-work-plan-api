package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleHistoryEntity
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
        createdBy = createdBy!!,
        createdAt = createdAt!!,
        lastUpdatedBy = updatedBy!!,
        lastUpdatedAt = updatedAt!!,
        exemptionReason = exemptionReason,
        lastUpdatedAtPrison = updatedAtPrison,
        createdAtPrison = createdAtPrison,
      )
    }

  fun fromScheduleHistoryEntityToDomain(entity: InductionScheduleHistoryEntity): InductionScheduleHistory =
    with(entity) {
      InductionScheduleHistory(
        reference = reference,
        prisonNumber = prisonNumber,
        deadlineDate = deadlineDate,
        scheduleCalculationRule = toInductionScheduleCalculationRule(scheduleCalculationRule),
        scheduleStatus = toInductionScheduleStatus(scheduleStatus),
        createdBy = createdBy!!,
        createdAt = createdAt!!,
        lastUpdatedBy = updatedBy!!,
        lastUpdatedAt = updatedAt!!,
        exemptionReason = exemptionReason,
        version = version,
        lastUpdatedAtPrison = updatedAtPrison,
        createdAtPrison = createdAtPrison,
      )
    }

  fun fromCreateDtoToEntity(createInductionScheduleDto: CreateInductionScheduleDto): InductionScheduleEntity =
    with(createInductionScheduleDto) {
      InductionScheduleEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        deadlineDate = deadlineDate,
        scheduleCalculationRule = toInductionScheduleCalculationRule(scheduleCalculationRule),
        scheduleStatus = toInductionScheduleStatus(scheduleStatus),
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      )
    }

  fun toInductionScheduleCalculationRule(inductionScheduleCalculationRule: InductionScheduleCalculationRuleEntity): InductionScheduleCalculationRuleDomain =
    when (inductionScheduleCalculationRule) {
      InductionScheduleCalculationRuleEntity.NEW_PRISON_ADMISSION -> InductionScheduleCalculationRuleDomain.NEW_PRISON_ADMISSION
      InductionScheduleCalculationRuleEntity.EXISTING_PRISONER -> InductionScheduleCalculationRuleDomain.EXISTING_PRISONER
    }

  fun toInductionScheduleCalculationRule(inductionScheduleCalculationRule: InductionScheduleCalculationRuleDomain): InductionScheduleCalculationRuleEntity =
    when (inductionScheduleCalculationRule) {
      InductionScheduleCalculationRuleDomain.NEW_PRISON_ADMISSION -> InductionScheduleCalculationRuleEntity.NEW_PRISON_ADMISSION
      InductionScheduleCalculationRuleDomain.EXISTING_PRISONER -> InductionScheduleCalculationRuleEntity.EXISTING_PRISONER
    }

  private fun toInductionScheduleStatus(inductionScheduleStatus: InductionScheduleStatusEntity): InductionScheduleStatusDomain =
    when (inductionScheduleStatus) {
      InductionScheduleStatusEntity.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS -> InductionScheduleStatusDomain.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
      InductionScheduleStatusEntity.SCHEDULED -> InductionScheduleStatusDomain.SCHEDULED
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
      InductionScheduleStatusEntity.EXEMPT_PRISONER_MERGE -> InductionScheduleStatusDomain.EXEMPT_PRISONER_MERGE
      InductionScheduleStatusEntity.EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS -> InductionScheduleStatusDomain.EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS
      InductionScheduleStatusEntity.EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE -> InductionScheduleStatusDomain.EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE
      InductionScheduleStatusEntity.COMPLETED -> InductionScheduleStatusDomain.COMPLETED
    }

  fun toInductionScheduleStatus(inductionScheduleStatus: InductionScheduleStatusDomain): InductionScheduleStatusEntity =
    when (inductionScheduleStatus) {
      InductionScheduleStatusDomain.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS -> InductionScheduleStatusEntity.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
      InductionScheduleStatusDomain.SCHEDULED -> InductionScheduleStatusEntity.SCHEDULED
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
      InductionScheduleStatusDomain.EXEMPT_PRISONER_MERGE -> InductionScheduleStatusEntity.EXEMPT_PRISONER_MERGE
      InductionScheduleStatusDomain.EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS -> InductionScheduleStatusEntity.EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS
      InductionScheduleStatusDomain.EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE -> InductionScheduleStatusEntity.EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE
      InductionScheduleStatusDomain.COMPLETED -> InductionScheduleStatusEntity.COMPLETED
    }
}
