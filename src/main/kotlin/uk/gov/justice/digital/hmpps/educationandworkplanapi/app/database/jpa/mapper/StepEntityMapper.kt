package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateStepDto

@Mapper
interface StepEntityMapper {

  /**
   * Maps the supplied [CreateStepDto] into a new un-persisted [StepEntity].
   * A new reference number is generated and mapped. The JPA managed fields are not mapped.
   * This method is suitable for creating a new [StepEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFields
  @GenerateNewReference
  @Mapping(target = "targetDate", ignore = true)
  fun fromDomainDtoToEntity(createStepDto: CreateStepDto): StepEntity

  /**
   * Maps the supplied [Step] into a new un-persisted [StepEntity].
   * The JPA managed fields are not mapped.
   * This method is suitable for creating a new [StepEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFields
  @Mapping(target = "targetDate", ignore = true)
  fun fromDomainToEntity(step: Step): StepEntity

  /**
   * Maps the supplied [StepEntity] into the domain [Step].
   */
  fun fromEntityToDomain(stepEntity: StepEntity): Step

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @Mapping(target = "targetDate", ignore = true)
  fun updateEntityFromDomain(@MappingTarget stepEntity: StepEntity, updatedStep: Step)
}
