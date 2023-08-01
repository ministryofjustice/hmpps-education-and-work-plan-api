package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step

@Mapper
abstract class StepEntityMapper {

  /**
   * Maps the supplied [Step] into a new un-persisted [StepEntity].
   * The JPA managed fields are not mapped.
   * This method is suitable for creating a new [StepEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFields
  @Mapping(target = "targetDate", ignore = true)
  abstract fun fromDomainToEntity(step: Step): StepEntity

  /**
   * Maps the supplied [StepEntity] into the domain [Step].
   */
  abstract fun fromEntityToDomain(stepEntity: StepEntity): Step

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @Mapping(target = "targetDate", ignore = true)
  abstract fun updateEntityFromDomain(@MappingTarget stepEntity: StepEntity, updatedStep: Step)
}
