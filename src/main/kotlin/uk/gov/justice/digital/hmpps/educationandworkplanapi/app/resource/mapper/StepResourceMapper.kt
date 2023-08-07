package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateStepRequest
import java.util.UUID

@Mapper(
  imports = [
    UUID::class,
  ],
)
abstract class StepResourceMapper {
  @Mapping(target = "reference", expression = "java( generateNewReference() )")
  @Mapping(target = "status", constant = "NOT_STARTED")
  abstract fun fromModelToDomain(createStepRequest: CreateStepRequest): Step

  @Mapping(target = "reference", expression = "java( existingReferenceElseNewReference(updateStepRequest) )")
  abstract fun fromModelToDomain(updateStepRequest: UpdateStepRequest): Step

  @Mapping(target = "stepReference", source = "reference")
  abstract fun fromDomainToModel(stepDomain: Step): StepResponse

  protected fun generateNewReference(): UUID = UUID.randomUUID()

  protected fun existingReferenceElseNewReference(updateStepRequest: UpdateStepRequest): UUID =
    updateStepRequest.stepReference ?: generateNewReference()
}
