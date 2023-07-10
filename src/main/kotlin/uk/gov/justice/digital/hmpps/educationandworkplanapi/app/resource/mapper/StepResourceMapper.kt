package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepResponse
import java.util.UUID

@Mapper(
  imports = [
    UUID::class,
  ],
)
interface StepResourceMapper {
  @Mapping(target = "reference", expression = "java(UUID.randomUUID())")
  @Mapping(target = "status", constant = "NOT_STARTED")
  fun fromModelToDomain(createStepRequest: CreateStepRequest): Step

  @Mapping(target = "stepReference", source = "reference")
  // TODO - RR-81 - map once targetDateRange have been added to the domain model etc
  @Mapping(target = "targetDateRange", constant = "ZERO_TO_THREE_MONTHS")
  fun fromDomainToModel(stepDomain: Step): StepResponse
}
