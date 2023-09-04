package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateGoalDto

@Mapper(
  uses = [
    StepEntityMapper::class,
  ],
)
abstract class GoalEntityMapper {

  @Autowired
  private lateinit var stepEntityMapper: StepEntityMapper

  /**
   * Maps the supplied [CreateGoalDto] into a new un-persisted [GoalEntity].
   * A new reference number is generated and mapped. The JPA managed fields are not mapped.
   * This method is suitable for creating a new [GoalEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  abstract fun fromDomainDtoToEntity(createGoalDto: CreateGoalDto): GoalEntity

  /**
   * Maps the supplied [GoalEntity] into the domain [Goal].
   */
  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  abstract fun fromEntityToDomain(goalEntity: GoalEntity): Goal

  /**
   * Updates the supplied [GoalEntity] with fields from the supplied [Goal]. The updated [GoalEntity] can then be
   * persisted to the database.
   */
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "steps", expression = "java( updateSteps(goalEntity, updatedGoal) )")
  abstract fun updateEntityFromDomain(@MappingTarget goalEntity: GoalEntity, updatedGoal: Goal)

  protected fun updateSteps(goalEntity: GoalEntity, updatedGoal: Goal): List<StepEntity> {
    val stepEntities = goalEntity.steps!!
    val updatedSteps = updatedGoal.steps

    updateExistingSteps(stepEntities, updatedSteps)
    addNewSteps(stepEntities, updatedSteps)
    removeSteps(stepEntities, updatedSteps)

    stepEntities.sortBy { it.sequenceNumber }

    return stepEntities
  }

  /**
   * Update the [StepEntity] whose reference number matches the corresponding [Step]
   */
  private fun updateExistingSteps(stepEntities: MutableList<StepEntity>, updatedSteps: List<Step>) {
    val updatedStepReferences = updatedSteps.map { it.reference }
    stepEntities
      .filter { stepEntity -> updatedStepReferences.contains(stepEntity.reference) }
      .onEach { stepEntity -> stepEntityMapper.updateEntityFromDomain(stepEntity, updatedSteps.first { updatedStep -> updatedStep.reference == stepEntity.reference }) }
  }

  /**
   * Add new [StepEntity]s from the list of updated [Step]s where the reference number is not present in the list of [StepEntity]s
   */
  private fun addNewSteps(stepEntities: MutableList<StepEntity>, updatedSteps: List<Step>) {
    val currentStepEntityReferences = stepEntities.map { it.reference }
    stepEntities.addAll(
      updatedSteps
        .filter { domainStep -> !currentStepEntityReferences.contains(domainStep.reference) }
        .map { domainStep -> stepEntityMapper.fromDomainToEntity(domainStep) },
    )
  }

  /**
   * Remove any [StepEntity]s whose reference number is not in the list of updated [Step]s
   */
  private fun removeSteps(stepEntities: MutableList<StepEntity>, updatedSteps: List<Step>) {
    val updatedStepReferences = updatedSteps.map { it.reference }
    stepEntities.removeIf { stepEntity ->
      !updatedStepReferences.contains(stepEntity.reference)
    }
  }
}
