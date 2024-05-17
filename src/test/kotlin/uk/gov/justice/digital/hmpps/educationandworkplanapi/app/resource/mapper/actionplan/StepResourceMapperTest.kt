package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidStep
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateStepDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidStepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateStepRequest
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus as StepStatusApi

@ExtendWith(MockitoExtension::class)
internal class StepResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: StepResourceMapperImpl

  @Test
  fun `should map from CreateStepRequest model to DTO`() {
    // Given
    val createStepRequest = aValidCreateStepRequest()

    val expectedCreateStepDto = aValidCreateStepDto(
      title = createStepRequest.title,
      status = StepStatus.NOT_STARTED,
      sequenceNumber = createStepRequest.sequenceNumber,
    )

    // When
    val actual = mapper.fromModelToDto(createStepRequest)

    // Then
    assertThat(actual).isEqualTo(expectedCreateStepDto)
  }

  @Test
  fun `should map from UpdateStepRequest model to DTO given update step request has a reference`() {
    // Given
    val stepReference = UUID.randomUUID()
    val updateStepRequest = aValidUpdateStepRequest(
      stepReference = stepReference,
      status = StepStatusApi.ACTIVE,
    )

    val expectedUpdateStepDto = aValidUpdateStepDto(
      reference = stepReference,
      title = updateStepRequest.title,
      status = StepStatus.ACTIVE,
      sequenceNumber = updateStepRequest.sequenceNumber,
    )

    // When
    val actual = mapper.fromModelToDto(updateStepRequest)

    // Then
    assertThat(actual).isEqualTo(expectedUpdateStepDto)
  }

  @Test
  fun `should map from UpdateStepRequest model to DTO given update step request does not have a reference`() {
    // Given
    val updateStepRequest = aValidUpdateStepRequest(
      stepReference = null,
      status = StepStatusApi.ACTIVE,
    )

    val expectedUpdateStepDto = aValidUpdateStepDto(
      reference = null,
      title = updateStepRequest.title,
      status = StepStatus.ACTIVE,
      sequenceNumber = updateStepRequest.sequenceNumber,
    )

    // When
    val actual = mapper.fromModelToDto(updateStepRequest)

    // Then
    assertThat(actual).isEqualTo(expectedUpdateStepDto)
  }

  @Test
  fun `should map from domain to model`() {
    // Given
    val step = aValidStep()
    val expected = aValidStepResponse(
      reference = step.reference,
      title = step.title,
      status = StepStatusApi.NOT_STARTED,
      sequenceNumber = step.sequenceNumber,
    )

    // When
    val actual = mapper.fromDomainToModel(step)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
  }
}
