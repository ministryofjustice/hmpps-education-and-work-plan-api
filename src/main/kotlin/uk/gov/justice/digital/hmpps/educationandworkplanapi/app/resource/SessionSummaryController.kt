package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Pattern
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_ID_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSummaryResponse

@RestController
class SessionSummaryController {

  @GetMapping("/session/{prisonId}/summary")
  fun getSessionSummary(@PathVariable @Pattern(regexp = PRISON_ID_FORMAT) prisonId: String): SessionSummaryResponse {
    TODO()
  }
}
