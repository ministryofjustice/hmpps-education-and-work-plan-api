package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonSearchResult
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonerSearchCriteria

@Service
class PrisonerSearchService {
  fun searchPrisoners(prisonId: String, searchCriteria: PrisonerSearchCriteria): ResponseEntity<PersonSearchResult> {
    TODO("Not yet implemented")
  }
}
