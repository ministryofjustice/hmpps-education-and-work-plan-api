package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.MissingReceptionDateException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.MissingSentenceStartDateException
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Service
class AssessmentService(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  @Value("\${pes.contract-start-date}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private val pesContractStartDate: LocalDate,
) {
  fun requireBasicSkillsAssessment(prisonNumber: String): Boolean {
    log.info { "Checking eligibility of basic skills assessment for prisoner [$prisonNumber]" }

    return prisonerSearchApiService.getPrisoner(prisonNumber).let {
      when {
        (it.receptionDate ?: throw MissingReceptionDateException(prisonNumber)).isBefore(pesContractStartDate) -> false
        (it.sentenceStartDate ?: throw MissingSentenceStartDateException(prisonNumber)).isBefore(pesContractStartDate) -> false

        else -> true
      }
    }
  }
}
