package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import java.time.Instant

@Mapper(
  imports = [
    Instant::class,
  ],
)
interface StepEntityMapper {

  @Mapping(target = "id", expression = "java( null )")
  @Mapping(target = "createdAt", expression = "java( null )")
  @Mapping(target = "updatedAt", expression = "java( null )")
  fun fromDomainToEntity(step: Step): StepEntity

  fun fromEntityToDomain(stepEntity: StepEntity): Step
}
