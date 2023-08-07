package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidStepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidUpdateStepRequest
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus as StepStatusApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange as TargetDateRangeApi

@ExtendWith(MockitoExtension::class)
internal class StepResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: StepResourceMapperImpl

  @Test
  fun `should map from CreateStepRequest model to domain`() {
    // Given
    val createStepRequest = aValidCreateStepRequest(
      targetDateRange = TargetDateRangeApi.ZERO_TO_THREE_MONTHS,
    )

    val expectedStep = aValidStep(
      title = createStepRequest.title,
      targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
      status = StepStatus.NOT_STARTED,
      sequenceNumber = createStepRequest.sequenceNumber,
    )

    // When
    val actual = mapper.fromModelToDomain(createStepRequest)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("reference").isEqualTo(expectedStep)
    assertThat(actual.reference).isNotNull()
  }

  @Test
  fun `should map from UpdateStepRequest model to domain given existing step with reference number`() {
    // Given
    val stepReference = UUID.randomUUID()
    val updateStepRequest = aValidUpdateStepRequest(
      stepReference = stepReference,
      targetDateRange = TargetDateRangeApi.SIX_TO_TWELVE_MONTHS,
      status = StepStatusApi.COMPLETE,
    )

    val expectedStep = aValidStep(
      reference = stepReference,
      title = updateStepRequest.title,
      targetDateRange = TargetDateRange.SIX_TO_TWELVE_MONTHS,
      status = StepStatus.COMPLETE,
      sequenceNumber = updateStepRequest.sequenceNumber,
    )

    // When
    val actual = mapper.fromModelToDomain(updateStepRequest)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expectedStep)
  }

  @Test
  fun `should map from UpdateStepRequest model to domain given new step without reference number`() {
    // Given
    val updateStepRequest = aValidUpdateStepRequest(
      stepReference = null,
      targetDateRange = TargetDateRangeApi.SIX_TO_TWELVE_MONTHS,
      status = StepStatusApi.COMPLETE,
    )

    val expectedStep = aValidStep(
      title = updateStepRequest.title,
      targetDateRange = TargetDateRange.SIX_TO_TWELVE_MONTHS,
      status = StepStatus.COMPLETE,
      sequenceNumber = updateStepRequest.sequenceNumber,
    )

    // When
    val actual = mapper.fromModelToDomain(updateStepRequest)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("reference").isEqualTo(expectedStep)
    assertThat(actual.reference).isNotNull()
  }

  @Test
  fun `should map from domain to model`() {
    // Given
    val step = aValidStep()
    val expected = aValidStepResponse(
      reference = step.reference,
      title = step.title,
      targetDateRange = TargetDateRangeApi.ZERO_TO_THREE_MONTHS,
      status = StepStatusApi.NOT_STARTED,
      sequenceNumber = step.sequenceNumber,
    )

    // When
    val actual = mapper.fromDomainToModel(step)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
  }
}
