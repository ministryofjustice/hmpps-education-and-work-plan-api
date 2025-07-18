package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.MissingSentenceStartDateAndReceptionDateException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.PrisonEducationServiceProperties

private val log = KotlinLogging.logger {}

@Service
class AssessmentService(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val pes: PrisonEducationServiceProperties,
) {
  fun requireBasicSkillsAssessment(prisonNumber: String): Boolean {
    log.info { "Checking eligibility of basic skills assessment for prisoner [$prisonNumber]" }

    return prisonerSearchApiService.getPrisoner(prisonNumber).let {
      val referenceDate = it.sentenceStartDate ?: it.receptionDate
      when {
        referenceDate == null -> throw MissingSentenceStartDateAndReceptionDateException(prisonNumber)
        referenceDate.isBefore(pes.contractStartDate) -> false

        else -> true
      }
    }
  }
}
