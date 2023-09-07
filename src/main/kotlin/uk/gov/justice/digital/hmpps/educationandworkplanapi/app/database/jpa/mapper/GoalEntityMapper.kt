package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateStepDto
import java.util.UUID

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
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  abstract fun fromDtoToEntity(createGoalDto: CreateGoalDto): GoalEntity

  /**
   * Maps the supplied [GoalEntity] into the domain [Goal].
   */
  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  abstract fun fromEntityToDomain(goalEntity: GoalEntity): Goal

  /**
   * Updates the supplied [GoalEntity] with fields from the supplied [UpdateGoalDtp]. The updated [GoalEntity] can then be
   * persisted to the database.
   */
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "steps", expression = "java( updateSteps(goalEntity, updatedGoalDto) )")
  abstract fun updateEntityFromDto(@MappingTarget goalEntity: GoalEntity, updatedGoalDto: UpdateGoalDto)

  protected fun updateSteps(goalEntity: GoalEntity, updatedGoalDto: UpdateGoalDto): List<StepEntity> {
    val stepEntities = goalEntity.steps!!
    val updatedSteps = updatedGoalDto.steps.setReferenceOnNewSteps()

    updateExistingSteps(stepEntities, updatedSteps)
    addNewSteps(stepEntities, updatedSteps)
    removeSteps(stepEntities, updatedSteps)

    stepEntities.sortBy { it.sequenceNumber }

    return stepEntities
  }

  /**
   * Update the [StepEntity] whose reference number matches the corresponding [UpdateGoalDto]
   */
  private fun updateExistingSteps(stepEntities: MutableList<StepEntity>, updates: List<UpdateStepDto>) {
    val updatedStepReferences = updates.map { it.reference }
    stepEntities
      .filter { stepEntity -> updatedStepReferences.contains(stepEntity.reference) }
      .onEach { stepEntity ->
        stepEntityMapper.updateEntityFromDto(
          stepEntity,
          updates.first { updatedStepDto -> updatedStepDto.reference == stepEntity.reference },
        )
      }
  }

  /**
   * Add new [StepEntity]s from the list of updated [UpdateStepDto]s where the reference number is not present in the list of [StepEntity]s
   */
  private fun addNewSteps(stepEntities: MutableList<StepEntity>, updates: List<UpdateStepDto>) {
    val currentStepEntityReferences = stepEntities.map { it.reference }
    stepEntities.addAll(
      updates
        .filter { updatedStepDto -> !currentStepEntityReferences.contains(updatedStepDto.reference) }
        .map { newStepDto -> stepEntityMapper.fromDtoToEntity(newStepDto) },
    )
  }

  /**
   * Remove any [StepEntity]s whose reference number is not in the list of updated [UpdateStepDto]s
   */
  private fun removeSteps(stepEntities: MutableList<StepEntity>, updates: List<UpdateStepDto>) {
    val updatedStepReferences = updates.map { it.reference }
    stepEntities.removeIf { stepEntity ->
      !updatedStepReferences.contains(stepEntity.reference)
    }
  }

  private fun List<UpdateStepDto>.setReferenceOnNewSteps(): List<UpdateStepDto> =
    this.map {
      if (it.reference == null) {
        it.copy(reference = UUID.randomUUID())
      } else {
        it
      }
    }
}
