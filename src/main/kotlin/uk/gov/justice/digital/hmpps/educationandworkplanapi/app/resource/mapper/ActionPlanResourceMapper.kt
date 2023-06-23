package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse

@Mapper(
  uses = [
    GoalResourceMapper::class,
  ],
)
interface ActionPlanResourceMapper {
  fun fromDomainToModel(actionPlan: ActionPlan): ActionPlanResponse
}
