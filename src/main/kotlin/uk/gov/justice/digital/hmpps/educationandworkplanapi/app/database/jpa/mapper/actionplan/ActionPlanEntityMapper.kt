package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.domain.goal.ActionPlanSummary
import uk.gov.justice.digital.hmpps.domain.goal.dto.CreateActionPlanDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ActionPlanSummaryProjection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference

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
