package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Step
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateStepDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateStepRequest
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus as DomainStepStatus

@Component
class StepResourceMapper {
  fun fromModelToDto(createStepRequest: CreateStepRequest) = with(createStepRequest) {
    CreateStepDto(title = title, sequenceNumber = sequenceNumber)
  }

  fun fromModelToDto(updateStepRequest: UpdateStepRequest) = with(updateStepRequest) {
    UpdateStepDto(
      title = title,
      sequenceNumber = sequenceNumber,
      reference = stepReference,
      status = toStepStatus(status),
    )
  }

  fun fromDomainToModel(stepDomain: Step) = with(stepDomain) {
    StepResponse(
      title = title,
      sequenceNumber = sequenceNumber,
      stepReference = reference,
      status = toStepStatus(status),
    )
  }

  protected fun toStepStatus(stepStatus: StepStatus): DomainStepStatus = when (stepStatus) {
    StepStatus.NOT_STARTED -> DomainStepStatus.NOT_STARTED
    StepStatus.ACTIVE -> DomainStepStatus.ACTIVE
    StepStatus.COMPLETE -> DomainStepStatus.COMPLETE
  }

  protected fun toStepStatus(stepStatus: DomainStepStatus): StepStatus = when (stepStatus) {
    DomainStepStatus.NOT_STARTED -> StepStatus.NOT_STARTED
    DomainStepStatus.ACTIVE -> StepStatus.ACTIVE
    DomainStepStatus.COMPLETE -> StepStatus.COMPLETE
  }
}
