package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule as ReviewScheduleCalculationRuleDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus as ReviewScheduleStatusDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleCalculationRule as ReviewScheduleCalculationRuleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus as ReviewScheduleStatusEntity

@Component
class ReviewScheduleEntityMapper {

  fun fromEntityToDomain(reviewScheduleEntity: ReviewScheduleEntity): ReviewSchedule =
    with(reviewScheduleEntity) {
      ReviewSchedule(
        reference = reference,
        prisonNumber = prisonNumber,
        reviewScheduleWindow = ReviewScheduleWindow(earliestReviewDate, latestReviewDate),
        scheduleCalculationRule = toReviewScheduleCalculationRule(scheduleCalculationRule),
        scheduleStatus = toReviewScheduleStatus(scheduleStatus),
        createdBy = createdBy!!,
        createdAt = createdAt!!,
        createdAtPrison = createdAtPrison,
        lastUpdatedBy = updatedBy!!,
        lastUpdatedAt = updatedAt!!,
        lastUpdatedAtPrison = updatedAtPrison,
      )
    }

  fun fromDomainToEntity(createReviewScheduleDto: CreateReviewScheduleDto): ReviewScheduleEntity =
    with(createReviewScheduleDto) {
      ReviewScheduleEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        earliestReviewDate = reviewScheduleWindow.dateFrom,
        latestReviewDate = reviewScheduleWindow.dateTo,
        scheduleCalculationRule = toReviewScheduleCalculationRule(scheduleCalculationRule),
        scheduleStatus = toReviewScheduleStatus(scheduleStatus),
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      )
    }

  fun updateExistingEntityFromDto(entity: ReviewScheduleEntity, dto: UpdateReviewScheduleDto) =
    with(entity) {
      earliestReviewDate = dto.reviewScheduleWindow.dateFrom
      latestReviewDate = dto.reviewScheduleWindow.dateTo
      scheduleCalculationRule = toReviewScheduleCalculationRule(dto.scheduleCalculationRule)
      scheduleStatus = toReviewScheduleStatus(dto.scheduleStatus)
      updatedAtPrison = dto.prisonId
    }

  private fun toReviewScheduleCalculationRule(calculationRule: ReviewScheduleCalculationRuleEntity): ReviewScheduleCalculationRuleDomain =
    when (calculationRule) {
      ReviewScheduleCalculationRuleEntity.PRISONER_READMISSION -> ReviewScheduleCalculationRuleDomain.PRISONER_READMISSION
      ReviewScheduleCalculationRuleEntity.PRISONER_TRANSFER -> ReviewScheduleCalculationRuleDomain.PRISONER_TRANSFER
      ReviewScheduleCalculationRuleEntity.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE -> ReviewScheduleCalculationRuleDomain.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE
      ReviewScheduleCalculationRuleEntity.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleDomain.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleEntity.BETWEEN_3_AND_6_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleDomain.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleEntity.BETWEEN_6_AND_12_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleDomain.BETWEEN_6_AND_12_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleEntity.BETWEEN_12_AND_60_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleDomain.BETWEEN_12_AND_60_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleEntity.MORE_THAN_60_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleDomain.MORE_THAN_60_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleEntity.INDETERMINATE_SENTENCE -> ReviewScheduleCalculationRuleDomain.INDETERMINATE_SENTENCE
      ReviewScheduleCalculationRuleEntity.PRISONER_ON_REMAND -> ReviewScheduleCalculationRuleDomain.PRISONER_ON_REMAND
      ReviewScheduleCalculationRuleEntity.PRISONER_UN_SENTENCED -> ReviewScheduleCalculationRuleDomain.PRISONER_UN_SENTENCED
    }

  private fun toReviewScheduleCalculationRule(calculationRule: ReviewScheduleCalculationRuleDomain): ReviewScheduleCalculationRuleEntity =
    when (calculationRule) {
      ReviewScheduleCalculationRuleDomain.PRISONER_READMISSION -> ReviewScheduleCalculationRuleEntity.PRISONER_READMISSION
      ReviewScheduleCalculationRuleDomain.PRISONER_TRANSFER -> ReviewScheduleCalculationRuleEntity.PRISONER_TRANSFER
      ReviewScheduleCalculationRuleDomain.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE -> ReviewScheduleCalculationRuleEntity.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleEntity.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleEntity.BETWEEN_3_AND_6_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.BETWEEN_6_AND_12_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleEntity.BETWEEN_6_AND_12_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.BETWEEN_12_AND_60_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleEntity.BETWEEN_12_AND_60_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.MORE_THAN_60_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleEntity.MORE_THAN_60_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.INDETERMINATE_SENTENCE -> ReviewScheduleCalculationRuleEntity.INDETERMINATE_SENTENCE
      ReviewScheduleCalculationRuleDomain.PRISONER_ON_REMAND -> ReviewScheduleCalculationRuleEntity.PRISONER_ON_REMAND
      ReviewScheduleCalculationRuleDomain.PRISONER_UN_SENTENCED -> ReviewScheduleCalculationRuleEntity.PRISONER_UN_SENTENCED
    }

  private fun toReviewScheduleStatus(reviewScheduleStatus: ReviewScheduleStatusEntity): ReviewScheduleStatusDomain =
    when (reviewScheduleStatus) {
      ReviewScheduleStatusEntity.SCHEDULED -> ReviewScheduleStatusDomain.SCHEDULED
      ReviewScheduleStatusEntity.COMPLETED -> ReviewScheduleStatusDomain.COMPLETED
      ReviewScheduleStatusEntity.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY -> ReviewScheduleStatusDomain.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
      ReviewScheduleStatusEntity.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES -> ReviewScheduleStatusDomain.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
      ReviewScheduleStatusEntity.EXEMPT_PRISONER_FAILED_TO_ENGAGE -> ReviewScheduleStatusDomain.EXEMPT_PRISONER_FAILED_TO_ENGAGE
      ReviewScheduleStatusEntity.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED -> ReviewScheduleStatusDomain.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED
      ReviewScheduleStatusEntity.EXEMPT_PRISONER_SAFETY_ISSUES -> ReviewScheduleStatusDomain.EXEMPT_PRISONER_SAFETY_ISSUES
      ReviewScheduleStatusEntity.EXEMPT_PRISON_REGIME_CIRCUMSTANCES -> ReviewScheduleStatusDomain.EXEMPT_PRISON_REGIME_CIRCUMSTANCES
      ReviewScheduleStatusEntity.EXEMPT_PRISON_STAFF_REDEPLOYMENT -> ReviewScheduleStatusDomain.EXEMPT_PRISON_STAFF_REDEPLOYMENT
      ReviewScheduleStatusEntity.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE -> ReviewScheduleStatusDomain.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE
      ReviewScheduleStatusEntity.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF -> ReviewScheduleStatusDomain.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF
      ReviewScheduleStatusEntity.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> ReviewScheduleStatusDomain.EXEMPT_SYSTEM_TECHNICAL_ISSUE
      ReviewScheduleStatusEntity.EXEMPT_PRISONER_TRANSFER -> ReviewScheduleStatusDomain.EXEMPT_PRISONER_TRANSFER
      ReviewScheduleStatusEntity.EXEMPT_PRISONER_RELEASE -> ReviewScheduleStatusDomain.EXEMPT_PRISONER_RELEASE
      ReviewScheduleStatusEntity.EXEMPT_PRISONER_DEATH -> ReviewScheduleStatusDomain.EXEMPT_PRISONER_DEATH
    }

  private fun toReviewScheduleStatus(reviewScheduleStatus: ReviewScheduleStatusDomain): ReviewScheduleStatusEntity =
    when (reviewScheduleStatus) {
      ReviewScheduleStatusDomain.SCHEDULED -> ReviewScheduleStatusEntity.SCHEDULED
      ReviewScheduleStatusDomain.COMPLETED -> ReviewScheduleStatusEntity.COMPLETED
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY -> ReviewScheduleStatusEntity.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES -> ReviewScheduleStatusEntity.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_FAILED_TO_ENGAGE -> ReviewScheduleStatusEntity.EXEMPT_PRISONER_FAILED_TO_ENGAGE
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED -> ReviewScheduleStatusEntity.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_SAFETY_ISSUES -> ReviewScheduleStatusEntity.EXEMPT_PRISONER_SAFETY_ISSUES
      ReviewScheduleStatusDomain.EXEMPT_PRISON_REGIME_CIRCUMSTANCES -> ReviewScheduleStatusEntity.EXEMPT_PRISON_REGIME_CIRCUMSTANCES
      ReviewScheduleStatusDomain.EXEMPT_PRISON_STAFF_REDEPLOYMENT -> ReviewScheduleStatusEntity.EXEMPT_PRISON_STAFF_REDEPLOYMENT
      ReviewScheduleStatusDomain.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE -> ReviewScheduleStatusEntity.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE
      ReviewScheduleStatusDomain.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF -> ReviewScheduleStatusEntity.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF
      ReviewScheduleStatusDomain.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> ReviewScheduleStatusEntity.EXEMPT_SYSTEM_TECHNICAL_ISSUE
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_TRANSFER -> ReviewScheduleStatusEntity.EXEMPT_PRISONER_TRANSFER
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_RELEASE -> ReviewScheduleStatusEntity.EXEMPT_PRISONER_RELEASE
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_DEATH -> ReviewScheduleStatusEntity.EXEMPT_PRISONER_DEATH
    }
}
