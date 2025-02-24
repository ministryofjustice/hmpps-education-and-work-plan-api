package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.PrisonerSearchController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonSearchResult

@Service
class PrisonerSearchService {
  fun searchPrisoners(prisonId: String, searchCriteria: PrisonerSearchController.PrisonerSearchCriteria): ResponseEntity<PersonSearchResult> {
    TODO("Not yet implemented")
  }
}
