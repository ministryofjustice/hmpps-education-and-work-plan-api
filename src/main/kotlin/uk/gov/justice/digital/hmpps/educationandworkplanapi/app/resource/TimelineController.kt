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
import uk.gov.justice.digital.hmpps.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.timeline.TimelineResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import java.time.LocalDate
import java.time.ZoneId

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
    @RequestParam(required = false) inductions: Boolean?,
    @RequestParam(required = false) goals: Boolean?,
    @RequestParam(required = false) reviews: Boolean?,
    @RequestParam(required = false) prisonEvents: Boolean?,
    @RequestParam(required = false) prisonId: String?,
    @RequestParam(required = false) eventsSince: LocalDate?,
  ): TimelineResponse {
    val timeline = timelineService.getTimelineForPrisoner(prisonNumber)

    applyFilter(
      timeline,
      inductions ?: false,
      goals ?: false,
      reviews ?: false,
      prisonEvents ?: false,
      prisonId,
      eventsSince,
    )

    return timelineMapper.fromDomainToModel(timeline)
  }

  private fun applyFilter(
    timeline: Timeline,
    inductions: Boolean,
    goals: Boolean,
    reviews: Boolean,
    prisonEvents: Boolean,
    prisonId: String? = null,
    eventsSince: LocalDate? = null,
  ) {
    // If no filtering criteria are applied, return all events
    if (!inductions && !goals && !reviews && !prisonEvents && prisonId == null && eventsSince == null) {
      return
    }

    val eventsSinceInstant = eventsSince?.atStartOfDay(ZoneId.systemDefault())?.toInstant()

    timeline.events.removeIf { event ->
      val eventTypeMatches = inductions &&
        event.eventType.isInduction ||
        goals &&
        event.eventType.isGoal ||
        reviews &&
        event.eventType.isReview ||
        prisonEvents &&
        event.eventType.isPrisonEvent

      val prisonMatches = prisonId == null || event.prisonId == prisonId

      val eventSinceMatches = eventsSinceInstant == null || event.timestamp.isAfter(eventsSinceInstant)

      // Remove event where there are no matches
      !(eventTypeMatches && prisonMatches && eventSinceMatches)
    }
  }
}
