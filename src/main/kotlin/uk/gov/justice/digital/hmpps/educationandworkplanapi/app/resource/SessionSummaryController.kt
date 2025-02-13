package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Pattern
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_ID_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.SessionSummaryService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonerIdsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionResponses
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionStatusType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSummaryResponse

@RestController
@RequestMapping("/session")
class SessionSummaryController(private val sessionSummaryService: SessionSummaryService) {

  @GetMapping("/{prisonId}/summary")
  @PreAuthorize(HAS_EDIT_SESSIONS)
  fun getSessionSummary(@PathVariable @Pattern(regexp = PRISON_ID_FORMAT) prisonId: String): SessionSummaryResponse {
    return sessionSummaryService.getSessionSummaries(prisonId)
  }

  @PostMapping("/summary")
  @PreAuthorize(HAS_EDIT_SESSIONS)
  fun getSessionSummaries(
    @RequestParam(required = true) status: SessionStatusType,
    @RequestBody requestIds: PrisonerIdsRequest,
  ): SessionResponses {
    return sessionSummaryService.getSessions(status, requestIds)
  }
}
