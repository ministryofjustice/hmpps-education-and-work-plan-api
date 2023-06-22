package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step

@Mapper
interface StepEntityMapper {

  /**
   * Maps the supplied [Step] into a new un-persisted [StepEntity].
   * The JPA managed fields are not mapped.
   * This method is suitable for creating a new [StepEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFields
  fun fromDomainToEntity(step: Step): StepEntity

  /**
   * Maps the supplied [StepEntity] into the domain [Step].
   */
  fun fromEntityToDomain(stepEntity: StepEntity): Step
}
