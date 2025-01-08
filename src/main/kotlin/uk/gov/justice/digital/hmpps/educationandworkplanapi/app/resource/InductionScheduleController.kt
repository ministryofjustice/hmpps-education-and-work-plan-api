package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionHistoryScheduleResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionScheduleResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionSchedulesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateInductionScheduleStatusRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusModel

@RestController
@Validated
@RequestMapping(
  value = ["/inductions/{prisonNumber}/induction-schedule"],
  produces = [MediaType.APPLICATION_JSON_VALUE],
)
class InductionScheduleController(
  private val inductionService: InductionService,
  private val inductionScheduleService: InductionScheduleService,
  private val inductionScheduleMapper: InductionScheduleResourceMapper,
  private val inductionHistoryScheduleResourceMapper: InductionHistoryScheduleResourceMapper,
) {

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_INDUCTIONS)
  fun getInductionSchedule(@PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String): InductionScheduleResponse {
    val induction = runCatching {
      inductionService.getInductionForPrisoner(prisonNumber)
    }.getOrNull()

    val inductionSchedule = inductionScheduleService.getInductionScheduleForPrisoner(prisonNumber)
    return inductionScheduleMapper.toInductionResponse(inductionSchedule, induction)
  }

  @GetMapping("/history")
  @PreAuthorize(HAS_VIEW_INDUCTIONS)
  fun getActionPlanReviewSchedules(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ): InductionSchedulesResponse {
    val induction = runCatching {
      inductionService.getInductionForPrisoner(prisonNumber)
    }.getOrNull()
    val inductionSchedules = inductionScheduleService.getInductionScheduleHistoryForPrisoner(prisonNumber)
    return InductionSchedulesResponse(
      inductionSchedules = inductionSchedules.map {
        inductionHistoryScheduleResourceMapper.toInductionResponse(
          it,
          induction,
        )
      },
    )
  }

  @PutMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_INDUCTIONS)
  @Transactional
  fun updateInductionScheduleStatus(
    @Valid
    @RequestBody updateInductionScheduleStatusRequest: UpdateInductionScheduleStatusRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ) {
    inductionScheduleService.updateLatestInductionScheduleStatus(
      prisonNumber = prisonNumber,
      newStatus = toReviewScheduleStatus(updateInductionScheduleStatusRequest.status),
      exemptionReason = updateInductionScheduleStatusRequest.exemptionReason,
    )
  }

  private fun toReviewScheduleStatus(inductionScheduleStatus: InductionScheduleStatusModel): InductionScheduleStatus =
    when (inductionScheduleStatus) {
      InductionScheduleStatusModel.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS -> InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
      InductionScheduleStatusModel.SCHEDULED -> InductionScheduleStatus.SCHEDULED
      InductionScheduleStatusModel.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY -> InductionScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
      InductionScheduleStatusModel.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES -> InductionScheduleStatus.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
      InductionScheduleStatusModel.EXEMPT_PRISONER_FAILED_TO_ENGAGE -> InductionScheduleStatus.EXEMPT_PRISONER_FAILED_TO_ENGAGE
      InductionScheduleStatusModel.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED -> InductionScheduleStatus.EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED
      InductionScheduleStatusModel.EXEMPT_PRISONER_SAFETY_ISSUES -> InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES
      InductionScheduleStatusModel.EXEMPT_PRISON_REGIME_CIRCUMSTANCES -> InductionScheduleStatus.EXEMPT_PRISON_REGIME_CIRCUMSTANCES
      InductionScheduleStatusModel.EXEMPT_PRISON_STAFF_REDEPLOYMENT -> InductionScheduleStatus.EXEMPT_PRISON_STAFF_REDEPLOYMENT
      InductionScheduleStatusModel.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE -> InductionScheduleStatus.EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE
      InductionScheduleStatusModel.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF -> InductionScheduleStatus.EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF
      InductionScheduleStatusModel.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE
      InductionScheduleStatusModel.EXEMPT_PRISONER_TRANSFER -> InductionScheduleStatus.EXEMPT_PRISONER_TRANSFER
      InductionScheduleStatusModel.EXEMPT_PRISONER_RELEASE -> InductionScheduleStatus.EXEMPT_PRISONER_RELEASE
      InductionScheduleStatusModel.EXEMPT_PRISONER_DEATH -> InductionScheduleStatus.EXEMPT_PRISONER_DEATH
      InductionScheduleStatusModel.EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS -> InductionScheduleStatus.EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS
      InductionScheduleStatusModel.EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE -> InductionScheduleStatus.EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE
      InductionScheduleStatusModel.COMPLETED -> InductionScheduleStatus.COMPLETED
    }
}
