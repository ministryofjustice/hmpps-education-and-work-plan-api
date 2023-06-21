package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import java.time.Instant

@Mapper(
  imports = [
    Instant::class,
  ],
)
interface StepEntityMapper {

  @ExcludeJpaManagedFields
  fun fromDomainToEntity(step: Step): StepEntity

  fun fromEntityToDomain(stepEntity: StepEntity): Step
}
