package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanSummary
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateActionPlanDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest

@Component
class ActionPlanResourceMapper(private val goalResourceMapper: GoalResourceMapper) {

  fun fromModelToDto(prisonNumber: String, request: CreateActionPlanRequest) = with(request) {
    CreateActionPlanDto(
      prisonNumber = prisonNumber,
      goals = goals.map {
        goalResourceMapper.fromModelToDto(it)
      },
    )
  }

  fun fromDomainToModel(actionPlan: ActionPlan) = with(actionPlan) {
    ActionPlanResponse(
      reference = reference,
      prisonNumber = prisonNumber,
      goals = goals.map { goalResourceMapper.fromDomainToModel(it) },
    )
  }

  fun fromDomainToModel(actionPlanSummaries: List<ActionPlanSummary>) = actionPlanSummaries.map { ActionPlanSummaryResponse(it.reference, it.prisonNumber) }
}
