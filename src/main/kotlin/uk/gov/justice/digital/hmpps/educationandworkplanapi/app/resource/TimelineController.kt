package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.timeline.TimelineResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import java.time.LocalDate

@RestController
@Validated
@RequestMapping(value = ["/timelines"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TimelineController(
  private val timelineService: TimelineService,
  private val timelineMapper: TimelineResourceMapper,
) {

  @GetMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_TIMELINE)
  fun getTimeline(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @RequestParam(required = false) inductions: Boolean = false,
    @RequestParam(required = false) goals: Boolean = false,
    @RequestParam(required = false) reviews: Boolean = false,
    @RequestParam(required = false) prisonEvents: Boolean = false,
    @RequestParam(required = false) prisonId: String?,
    @RequestParam(required = false) eventsSince: LocalDate?,
  ): TimelineResponse {
    val timeline = timelineService.getTimelineForPrisoner(
      prisonNumber,
      inductions,
      goals,
      reviews,
      prisonEvents,
      prisonId,
      eventsSince,
    )

    return timelineMapper.fromDomainToModel(timeline)
  }
}
