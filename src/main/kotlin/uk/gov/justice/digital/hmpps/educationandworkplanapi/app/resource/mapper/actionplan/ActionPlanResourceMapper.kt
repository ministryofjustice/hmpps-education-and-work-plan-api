package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.domain.goal.ActionPlanSummary
import uk.gov.justice.digital.hmpps.domain.goal.dto.CreateActionPlanDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest

@Mapper(
  uses = [
    GoalResourceMapper::class,
  ],
)
interface ActionPlanResourceMapper {

  fun fromModelToDto(prisonNumber: String, request: CreateActionPlanRequest): CreateActionPlanDto

  fun fromDomainToModel(actionPlan: ActionPlan): ActionPlanResponse

  fun fromDomainToModel(actionPlanSummaries: List<ActionPlanSummary>): List<ActionPlanSummaryResponse>
}
