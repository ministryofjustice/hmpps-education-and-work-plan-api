package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan

import org.mapstruct.Mapper
import org.mapstruct.MappingTarget
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateStepDto

@Mapper
interface StepEntityMapper {

  /**
   * Maps the supplied [CreateStepDto] into a new un-persisted [StepEntity].
   * A new reference number is generated and mapped. The JPA managed fields are not mapped.
   * This method is suitable for creating a new [StepEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDtoToEntity(createStepDto: CreateStepDto): StepEntity

  /**
   * Maps the supplied [UpdateStepDto] into a new un-persisted [StepEntity].
   * The JPA managed fields are not mapped.
   * This method is suitable for creating a new [StepEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFields
  fun fromDtoToEntity(updateStepDto: UpdateStepDto): StepEntity

  /**
   * Maps the supplied [StepEntity] into the domain [Step].
   */
  fun fromEntityToDomain(stepEntity: StepEntity): Step

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  fun updateEntityFromDto(@MappingTarget stepEntity: StepEntity, updatedStep: UpdateStepDto)
}
