package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Step
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateStepDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus as StepStatusDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepStatus as StepStatusEntity

@Component
class StepEntityMapper : KeyAwareEntityMapper<StepEntity, Step> {

  /**
   * Maps the supplied [CreateStepDto] into a new un-persisted [StepEntity].
   * A new reference number is generated and mapped. The JPA managed fields are not mapped.
   * This method is suitable for creating a new [StepEntity] to be subsequently persisted to the database.
   */
  fun fromDtoToEntity(createStepDto: CreateStepDto): StepEntity = with(createStepDto) {
    StepEntity(
      reference = UUID.randomUUID(),
      title = title,
      sequenceNumber = sequenceNumber,
      status = toStepStatus(status),
    )
  }

  /**
   * Maps the supplied [UpdateStepDto] into a new un-persisted [StepEntity].
   * The JPA managed fields are not mapped.
   * This method is suitable for creating a new [StepEntity] to be subsequently persisted to the database.
   */
  fun fromDtoToEntity(updateStepDto: UpdateStepDto): StepEntity = with(updateStepDto) {
    StepEntity(
      reference = reference!!,
      title = title,
      sequenceNumber = sequenceNumber,
      status = toStepStatus(status),
    )
  }

  /**
   * Maps the supplied [StepEntity] into the domain [Step].
   */
  fun fromEntityToDomain(stepEntity: StepEntity): Step = with(stepEntity) {
    Step(
      reference = reference,
      title = title,
      sequenceNumber = sequenceNumber,
      status = toStepStatus(status),
    )
  }

  fun updateEntityFromDto(stepEntity: StepEntity, updatedStep: UpdateStepDto) = with(stepEntity) {
    title = updatedStep.title
    sequenceNumber = updatedStep.sequenceNumber
    status = toStepStatus(updatedStep.status)
  }

  fun fromDtoToDomain(updateStepDto: UpdateStepDto): Step = with(updateStepDto) {
    Step(
      reference = if (reference != null) reference!! else UUID.randomUUID(),
      title = title,
      sequenceNumber = sequenceNumber,
      status = status,
    )
  }

  override fun fromDomainToEntity(domain: Step): StepEntity = with(domain) {
    StepEntity(
      reference = reference,
      title = title,
      sequenceNumber = sequenceNumber,
      status = toStepStatus(status),
    )
  }

  override fun updateEntityFromDomain(entity: StepEntity, domain: Step) = with(entity) {
    title = domain.title
    sequenceNumber = domain.sequenceNumber
    status = toStepStatus(domain.status)
  }

  private fun toStepStatus(stepStatus: StepStatusDomain): StepStatusEntity = when (stepStatus) {
    StepStatusDomain.ACTIVE -> StepStatusEntity.ACTIVE
    StepStatusDomain.NOT_STARTED -> StepStatusEntity.NOT_STARTED
    StepStatusDomain.COMPLETE -> StepStatusEntity.COMPLETE
  }

  private fun toStepStatus(stepStatus: StepStatusEntity): StepStatusDomain = when (stepStatus) {
    StepStatusEntity.ACTIVE -> StepStatusDomain.ACTIVE
    StepStatusEntity.NOT_STARTED -> StepStatusDomain.NOT_STARTED
    StepStatusEntity.COMPLETE -> StepStatusDomain.COMPLETE
  }
}
