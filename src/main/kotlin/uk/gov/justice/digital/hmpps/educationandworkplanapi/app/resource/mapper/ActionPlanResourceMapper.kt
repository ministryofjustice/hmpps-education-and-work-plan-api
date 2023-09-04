package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateActionPlanDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest

@Mapper(
  uses = [
    GoalResourceMapper::class,
  ],
)
interface ActionPlanResourceMapper {

  fun fromModelToDomainDto(prisonNumber: String, request: CreateActionPlanRequest): CreateActionPlanDto

  fun fromDomainToModel(actionPlan: ActionPlan): ActionPlanResponse

  fun fromDomainToModel(actionPlanSummaries: List<ActionPlanSummary>): List<ActionPlanSummaryResponse>
}
