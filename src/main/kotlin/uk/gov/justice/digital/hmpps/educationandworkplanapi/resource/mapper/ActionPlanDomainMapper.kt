package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse

@Mapper(
  uses = [
    GoalDomainMapper::class,
  ],
)
interface ActionPlanDomainMapper {
  fun fromDomainToModel(actionPlan: ActionPlan): ActionPlanResponse
}
