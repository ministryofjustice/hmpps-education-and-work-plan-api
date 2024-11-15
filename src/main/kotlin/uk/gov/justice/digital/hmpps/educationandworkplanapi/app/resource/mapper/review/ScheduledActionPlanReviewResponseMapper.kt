package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ScheduledActionPlanReviewResponse
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule as ReviewScheduleCalculationRuleDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus as ReviewScheduleStatusDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleCalculationRule as ReviewScheduleCalculationRuleApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus as ReviewScheduleStatusApi

@Component
class ScheduledActionPlanReviewResponseMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {

  fun fromDomainToModel(reviewSchedule: ReviewSchedule): ScheduledActionPlanReviewResponse =
    with(reviewSchedule) {
      ScheduledActionPlanReviewResponse(
        reference = reference,
        reviewDateFrom = reviewScheduleWindow.dateFrom,
        reviewDateTo = reviewScheduleWindow.dateTo,
        status = toReviewScheduleStatus(scheduleStatus),
        calculationRule = toReviewScheduleCalculationRule(scheduleCalculationRule),
        createdBy = createdBy,
        createdByDisplayName = userService.getUserDetails(createdBy).name,
        createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
        createdAtPrison = createdAtPrison,
        updatedBy = lastUpdatedBy,
        updatedByDisplayName = userService.getUserDetails(lastUpdatedBy).name,
        updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
        updatedAtPrison = lastUpdatedAtPrison,
      )
    }

  private fun toReviewScheduleStatus(reviewStatus: ReviewScheduleStatusDomain): ReviewScheduleStatusApi =
    when (reviewStatus) {
      ReviewScheduleStatusDomain.SCHEDULED -> ReviewScheduleStatusApi.SCHEDULED
      ReviewScheduleStatusDomain.COMPLETED -> ReviewScheduleStatusApi.COMPLETED
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY -> ReviewScheduleStatusApi.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES -> ReviewScheduleStatusApi.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_FAILED_TO_ENGAGE -> ReviewScheduleStatusApi.EXEMPT_PRISONER_FAILED_TO_ENGAGE
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED -> ReviewScheduleStatusApi.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_SAFETY_ISSUES -> ReviewScheduleStatusApi.EXEMPT_PRISONER_SAFETY_ISSUES
      ReviewScheduleStatusDomain.EXEMPT_PRISON_REGIME_CIRCUMSTANCES -> ReviewScheduleStatusApi.EXEMPT_PRISON_REGIME_CIRCUMSTANCES
      ReviewScheduleStatusDomain.EXEMPT_PRISON_STAFF_REDEPLOYMENT -> ReviewScheduleStatusApi.EXEMPT_PRISON_STAFF_REDEPLOYMENT
      ReviewScheduleStatusDomain.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE -> ReviewScheduleStatusApi.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE
      ReviewScheduleStatusDomain.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF -> ReviewScheduleStatusApi.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF
      ReviewScheduleStatusDomain.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> ReviewScheduleStatusApi.EXEMPT_SYSTEM_TECHNICAL_ISSUE
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_TRANSFER -> ReviewScheduleStatusApi.EXEMPT_PRISONER_TRANSFER
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_RELEASE -> ReviewScheduleStatusApi.EXEMPT_PRISONER_RELEASE
      ReviewScheduleStatusDomain.EXEMPT_PRISONER_DEATH -> ReviewScheduleStatusApi.EXEMPT_PRISONER_DEATH
    }

  private fun toReviewScheduleCalculationRule(calculationRule: ReviewScheduleCalculationRuleDomain): ReviewScheduleCalculationRuleApi =
    when (calculationRule) {
      ReviewScheduleCalculationRuleDomain.PRISONER_READMISSION -> ReviewScheduleCalculationRuleApi.PRISONER_READMISSION
      ReviewScheduleCalculationRuleDomain.PRISONER_TRANSFER -> ReviewScheduleCalculationRuleApi.PRISONER_TRANSFER
      ReviewScheduleCalculationRuleDomain.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE -> ReviewScheduleCalculationRuleApi.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleApi.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleApi.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.BETWEEN_6_AND_12_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleApi.BETWEEN_6_AND_12_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.BETWEEN_12_AND_60_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleApi.BETWEEN_12_AND_60_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.MORE_THAN_60_MONTHS_TO_SERVE -> ReviewScheduleCalculationRuleApi.MORE_THAN_60_MONTHS_TO_SERVE
      ReviewScheduleCalculationRuleDomain.INDETERMINATE_SENTENCE -> ReviewScheduleCalculationRuleApi.INDETERMINATE_SENTENCE
      ReviewScheduleCalculationRuleDomain.PRISONER_ON_REMAND -> ReviewScheduleCalculationRuleApi.PRISONER_ON_REMAND
      ReviewScheduleCalculationRuleDomain.PRISONER_UN_SENTENCED -> ReviewScheduleCalculationRuleApi.PRISONER_UN_SENTENCED
    }
}
