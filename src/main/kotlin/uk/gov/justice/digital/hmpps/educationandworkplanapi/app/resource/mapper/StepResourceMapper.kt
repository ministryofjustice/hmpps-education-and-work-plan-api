package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepResponse
import java.util.UUID

@Mapper(
  imports = [
    UUID::class,
  ],
)
interface StepResourceMapper {
  // TODO - RR-3 - this will need to change when creating vs updating a Step
  @Mapping(target = "reference", expression = "java(UUID.randomUUID())")
  @Mapping(target = "status", constant = "NOT_STARTED")
  fun fromModelToDomain(stepRequest: StepRequest): Step

  @Mapping(target = "stepReference", source = "reference")
  fun fromDomainToModel(stepDomain: Step): StepResponse
}
