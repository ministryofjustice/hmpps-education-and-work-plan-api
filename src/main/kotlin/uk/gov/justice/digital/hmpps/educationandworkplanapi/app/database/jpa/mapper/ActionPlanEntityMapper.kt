package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ActionPlanSummaryProjection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateActionPlanDto

@Mapper(
  uses = [
    GoalEntityMapper::class,
  ],
)
interface ActionPlanEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDtoToEntity(createActionPlanDto: CreateActionPlanDto): ActionPlanEntity

  fun fromEntityToDomain(actionPlanEntity: ActionPlanEntity): ActionPlan
  fun fromEntitySummariesToDomainSummaries(actionPlanSummaryProjections: List<ActionPlanSummaryProjection>): List<ActionPlanSummary>
}
