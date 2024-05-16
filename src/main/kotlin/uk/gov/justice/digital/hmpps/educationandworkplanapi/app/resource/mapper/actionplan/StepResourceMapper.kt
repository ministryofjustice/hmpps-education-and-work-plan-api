package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.domain.goal.Step
import uk.gov.justice.digital.hmpps.domain.goal.dto.CreateStepDto
import uk.gov.justice.digital.hmpps.domain.goal.dto.UpdateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateStepRequest
import java.util.UUID

@Mapper(
  imports = [
    UUID::class,
  ],
)
interface StepResourceMapper {
  @Mapping(target = "status", constant = "NOT_STARTED")
  fun fromModelToDto(createStepRequest: CreateStepRequest): CreateStepDto

  @Mapping(target = "reference", source = "stepReference")
  fun fromModelToDto(updateStepRequest: UpdateStepRequest): UpdateStepDto

  @Mapping(target = "stepReference", source = "reference")
  fun fromDomainToModel(stepDomain: Step): StepResponse
}
