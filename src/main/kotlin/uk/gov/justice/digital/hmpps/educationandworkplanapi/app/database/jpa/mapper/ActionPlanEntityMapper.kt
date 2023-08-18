package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan

@Mapper(
  uses = [
    GoalEntityMapper::class,
  ],
)
interface ActionPlanEntityMapper {
  @ExcludeJpaManagedFields
  fun fromDomainToEntity(actionPlan: ActionPlan): ActionPlanEntity

  fun fromEntityToDomain(actionPlanEntity: ActionPlanEntity): ActionPlan
}
