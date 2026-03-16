package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Pattern
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AiGoalGenerator
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GeneratedGoal

@RestController
class AiGoalController(
  private val aiGoalGenerator: AiGoalGenerator,
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
  private val inductionService: InductionService,
) {

  @PostMapping("/ai-goal/{prisonNumber}")
  fun generateAiGoal(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ): GeneratedGoal? {
    val prisoner = prisonerSearchApiClient.getPrisoner(prisonNumber)
    val induction = inductionService.getInductionForPrisoner(prisonNumber)

    return aiGoalGenerator.generateGoal(prisoner, induction)
  }
}
