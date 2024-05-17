package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan

import org.mapstruct.AfterMapping
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Step
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateStepDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference

@Mapper(
  uses = [
    StepEntityMapper::class,
  ],
)
abstract class GoalEntityMapper {
  @Autowired
  private lateinit var stepEntityMapper: StepEntityMapper

  @Autowired
  private lateinit var entityListManager: GoalEntityListManager<StepEntity, Step>

  /**
   * Maps the supplied [CreateGoalDto] into a new un-persisted [GoalEntity].
   * A new reference number is generated and mapped. The JPA managed fields are not mapped.
   * This method is suitable for creating a new [GoalEntity] to be subsequently persisted to the database.
   */
  @BeanMapping(qualifiedByName = ["addNewStepsDuringCreate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "steps", ignore = true)
  abstract fun fromDtoToEntity(createGoalDto: CreateGoalDto): GoalEntity

  @Named("addNewStepsDuringCreate")
  @AfterMapping
  fun addNewStepsDuringCreate(dto: CreateGoalDto, @MappingTarget entity: GoalEntity) {
    addNewStepsToEntity(dto.steps, entity)
  }

  /**
   * Maps the supplied [GoalEntity] into the domain [Goal].
   */
  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  abstract fun fromEntityToDomain(goalEntity: GoalEntity): Goal

  /**
   * Updates the supplied [GoalEntity] with fields from the supplied [UpdateGoalDtp]. The updated [GoalEntity] can then be
   * persisted to the database.
   */
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "createdAtPrison", ignore = true)
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "steps", expression = "java( updateSteps(goalEntity, updatedGoalDto) )")
  abstract fun updateEntityFromDto(@MappingTarget goalEntity: GoalEntity, updatedGoalDto: UpdateGoalDto)

  protected fun updateSteps(entity: GoalEntity, dto: UpdateGoalDto): List<StepEntity> {
    val existingSteps = entity.steps!!
    val updatedSteps = dto.steps.map { stepEntityMapper.fromDtoToDomain(it) }

    entityListManager.updateExisting(existingSteps, updatedSteps, stepEntityMapper)
    entityListManager.addNew(entity, existingSteps, updatedSteps, stepEntityMapper)
    entityListManager.deleteRemoved(existingSteps, updatedSteps)

    existingSteps.sortBy { it.sequenceNumber }

    return existingSteps
  }

  private fun addNewStepsToEntity(steps: List<CreateStepDto>, entity: GoalEntity) {
    steps.forEach {
      entity.addChild(
        stepEntityMapper.fromDtoToEntity(it),
        entity.steps(),
      )
    }
  }
}
