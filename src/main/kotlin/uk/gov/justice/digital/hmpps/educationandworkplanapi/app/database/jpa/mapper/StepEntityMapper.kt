package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.MappingTarget
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import java.time.Instant

@Mapper(
  imports = [
    Instant::class,
  ],
)
interface StepEntityMapper {

  /**
   * Maps the supplied [Step] into a new un-persisted [StepEntity].
   * The JPA managed fields are not mapped.
   * This method is suitable for creating a new [StepEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFields
  fun fromDomainToEntity(step: Step): StepEntity

  /**
   * Updates an existing persisted [StepEntity] with fields from the supplied [Step].
   * This method is suitable for updating an existing (already persisted) [StepEntity] with fields from the domain [Step]
   */
  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  fun updateEntityFromDomain(@MappingTarget stepEntity: StepEntity, step: Step): StepEntity

  /**
   * Maps the supplied [StepEntity] into the domain [Step].
   */
  fun fromEntityToDomain(stepEntity: StepEntity): Step
}
