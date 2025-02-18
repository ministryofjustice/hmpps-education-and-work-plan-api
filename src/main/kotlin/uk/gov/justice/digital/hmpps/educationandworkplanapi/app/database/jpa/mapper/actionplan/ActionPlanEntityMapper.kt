package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanSummary
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateActionPlanDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ActionPlanSummaryProjection
import java.util.UUID

@Component
class ActionPlanEntityMapper(
  private val goalEntityMapper: GoalEntityMapper,
) {
  fun fromDtoToEntity(createActionPlanDto: CreateActionPlanDto): ActionPlanEntity = with(createActionPlanDto) {
    ActionPlanEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
    ).apply {
      goals.addAll(
        createActionPlanDto.goals.map { goalEntityMapper.fromDtoToEntity(it) },
      )
    }
  }

  fun fromEntityToDomain(actionPlanEntity: ActionPlanEntity): ActionPlan = with(actionPlanEntity) {
    ActionPlan(
      reference = reference,
      prisonNumber = prisonNumber,
      goals = goals.map { goalEntityMapper.fromEntityToDomain(it) },
    )
  }

  fun fromEntitySummariesToDomainSummaries(actionPlanSummaryProjections: List<ActionPlanSummaryProjection>): List<ActionPlanSummary> = actionPlanSummaryProjections.map {
    ActionPlanSummary(
      reference = it.reference,
      prisonNumber = it.prisonNumber,
    )
  }
}
