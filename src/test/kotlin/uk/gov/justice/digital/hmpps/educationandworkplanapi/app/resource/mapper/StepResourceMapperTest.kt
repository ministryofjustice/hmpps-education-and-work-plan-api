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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus as StepStatusApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange as TargetDateRangeApi

@ExtendWith(MockitoExtension::class)
internal class StepResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: StepResourceMapperImpl

  @Test
  fun `should map from model to domain`() {
    // Given
    val stepRequest = aValidCreateStepRequest()
    val expectedStep = aValidStep(
      title = stepRequest.title,
      targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
      status = StepStatus.NOT_STARTED,
      sequenceNumber = stepRequest.sequenceNumber,
    )

    // When
    val actual = mapper.fromModelToDomain(stepRequest)

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
