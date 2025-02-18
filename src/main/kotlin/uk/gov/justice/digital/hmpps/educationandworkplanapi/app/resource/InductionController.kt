package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNoReleaseDateForSentenceTypeException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ScheduleAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateInductionRequest

private val log = KotlinLogging.logger {}

@RestController
@Validated
@RequestMapping(value = ["/inductions"], produces = [MediaType.APPLICATION_JSON_VALUE])
class InductionController(
  private val inductionService: InductionService,
  private val inductionMapper: InductionResourceMapper,
  private val scheduleAdapter: ScheduleAdapter,
) {

  @PostMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_INDUCTIONS)
  @Transactional
  fun createInduction(
    @Valid
    @RequestBody
    request: CreateInductionRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ) {
    inductionService.createInduction(inductionMapper.toCreateInductionDto(prisonNumber, request))

    try {
      scheduleAdapter.completeInductionScheduleAndCreateInitialReviewSchedule(prisonNumber)
    } catch (e: ReviewScheduleNoReleaseDateForSentenceTypeException) {
      log.warn { "Induction created, but could not create initial Review Schedule: ${e.message}" }
    }
  }

  @GetMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_INDUCTIONS)
  fun getInduction(@PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String): InductionResponse = with(inductionService.getInductionForPrisoner(prisonNumber)) {
    inductionMapper.toInductionResponse(this)
  }

  @PutMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_INDUCTIONS)
  @Transactional
  fun updateInduction(
    @Valid
    @RequestBody
    request: UpdateInductionRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ) {
    inductionService.updateInduction(inductionMapper.toUpdateInductionDto(prisonNumber, request))
  }
}
