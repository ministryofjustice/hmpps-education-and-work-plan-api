package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.MissingReceptionDateException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.MissingSentenceStartDateException
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
      val receptionDate = it.receptionDate ?: throw MissingReceptionDateException(prisonNumber)
      val sentenceStartDate = it.sentenceStartDate ?: throw MissingSentenceStartDateException(prisonNumber)

      when {
        receptionDate.isBefore(pes.contractStartDate) -> false
        sentenceStartDate.isBefore(pes.contractStartDate) -> false

        else -> true
      }
    }
  }
}
