package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionHistoryScheduleResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionScheduleResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionSchedulesResponse

@RestController
@Validated
@RequestMapping(
  value = ["/inductions/{prisonNumber}/induction-schedule"],
  produces = [MediaType.APPLICATION_JSON_VALUE],
)
class InductionScheduleController(
  private val inductionService: InductionService,
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

    val inductionSchedule = inductionService.getInductionScheduleForPrisoner(prisonNumber)
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
    val inductionSchedules = inductionService.getInductionSchedulesForPrisoner(prisonNumber)
    return InductionSchedulesResponse(
      inductionSchedules = inductionSchedules.map {
        inductionHistoryScheduleResourceMapper.toInductionResponse(
          it,
          induction,
        )
      },
    )
  }
}
