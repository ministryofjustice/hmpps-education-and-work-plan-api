package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Pattern
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_ID_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.SessionSummaryService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSummaryResponse

@RestController
class SessionSummaryController(private val sessionSummaryService: SessionSummaryService) {

  @GetMapping("/session/{prisonId}/summary")
  @PreAuthorize(HAS_EDIT_SESSIONS)
  fun getSessionSummary(@PathVariable @Pattern(regexp = PRISON_ID_FORMAT) prisonId: String): SessionSummaryResponse {
    return sessionSummaryService.getSessionSummaries(prisonId)
  }
}
