package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GoalEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.GoalRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalPersistenceAdapter

@Component
class JpaGoalPersistenceAdapter(
  private val goalRepository: GoalRepository,
  private val goalMapper: GoalEntityMapper,
) : GoalPersistenceAdapter {
  override fun saveGoal(goal: Goal, prisonNumber: String): Goal {
    var entity = goalMapper.fromDomainToEntity(goal)
    entity = goalRepository.save(entity)
    return goalMapper.fromEntityToDomain(entity)
  }
}
