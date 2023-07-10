package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidStepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus as StepStatusApi

@ExtendWith(MockitoExtension::class)
internal class StepResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: StepResourceMapperImpl

  @Test
  fun `should map from model to domain`() {
    // Given
    val createStepRequest = aValidCreateStepRequest()
    val expectedStep = aValidStep(
      title = createStepRequest.title,
      targetDate = null,
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
