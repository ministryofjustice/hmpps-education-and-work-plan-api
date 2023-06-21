package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step

@Mapper(
  uses = [
    StepEntityMapper::class,
  ],
)
abstract class GoalEntityMapper {

  @Autowired
  protected lateinit var stepEntityMapper: StepEntityMapper

  /**
   * Maps the supplied [Goal] into a new un-persisted [GoalEntity].
   * The JPA managed fields are not mapped.
   * This method is suitable for creating a new [GoalEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFields
  abstract fun fromDomainToEntity(goal: Goal): GoalEntity

  /**
   * Updates an existing persisted [GoalEntity] with fields from the supplied [Goal].
   * This method is suitable for updating an existing (already persisted) [GoalEntity] with fields from the domain [Goal]
   */
  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @Mapping(target = "steps", expression = "java( updateStepEntitiesFromDomain(goalEntity.getSteps(), goal.getSteps()) )")
  abstract fun updateEntityFromDomain(@MappingTarget goalEntity: GoalEntity, goal: Goal): GoalEntity

  /**
   * Maps the supplied [GoalEntity] into the domain [Goal].
   */
  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  abstract fun fromEntityToDomain(goalEntity: GoalEntity): Goal

  protected fun updateStepEntitiesFromDomain(entitySteps: MutableList<StepEntity>, domainSteps: MutableList<Step>): MutableList<StepEntity> {
    val stepsToUpdate = domainSteps.filter { domainStep -> domainStep.reference in entitySteps.map { it.reference } }
    val stepsToAdd = domainSteps.filter { domainStep -> domainStep.reference !in entitySteps.map { it.reference } }
    val stepsToDelete = entitySteps.filter { entityStep -> entityStep.reference !in domainSteps.map { it.reference } }

    entitySteps.removeAll(stepsToDelete)

    stepsToUpdate.forEach { domainStep ->
      val entityStep = entitySteps.find { it.reference == domainStep.reference }
      stepEntityMapper.updateEntityFromDomain(entityStep!!, domainStep)
    }

    stepsToAdd.forEach { domainStep ->
      entitySteps.add(stepEntityMapper.fromDomainToEntity(domainStep))
    }

    return entitySteps
  }
}
