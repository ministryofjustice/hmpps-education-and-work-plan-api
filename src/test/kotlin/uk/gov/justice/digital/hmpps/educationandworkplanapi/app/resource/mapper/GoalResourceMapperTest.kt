package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidGoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidStepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidUpdateStepRequest
import java.time.LocalDate
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus as GoalStatusApi

@ExtendWith(MockitoExtension::class)
internal class GoalResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: GoalResourceMapperImpl

  @Mock
  private lateinit var stepMapper: StepResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map from CreateStepRequest model to domain`() {
    // Given
    val createStepRequest = aValidCreateStepRequest()
    val createGoalRequest = aValidCreateGoalRequest(
      reviewDate = LocalDate.now(),
      steps = mutableListOf(createStepRequest),
    )

    val expectedStep = aValidCreateStepDto()
    given(stepMapper.fromModelToDto(any())).willReturn(expectedStep)

    val expectedGoal = aValidCreateGoalDto(
      title = createGoalRequest.title,
      reviewDate = createGoalRequest.reviewDate,
      status = GoalStatus.ACTIVE,
      notes = createGoalRequest.notes,
      steps = mutableListOf(expectedStep),
    )

    // When
    val actual = mapper.fromModelToDto(createGoalRequest)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expectedGoal)
    verify(stepMapper).fromModelToDto(createStepRequest)
  }

  @Test
  fun `should map from UpdateStepRequest model to domain`() {
    // Given
    val updateStepRequest = aValidUpdateStepRequest()
    val updateGoalRequest = aValidUpdateGoalRequest(
      reviewDate = LocalDate.now(),
      status = GoalStatusApi.COMPLETED,
      steps = mutableListOf(updateStepRequest),
    )

    val expectedStep = aValidStep()
    given(stepMapper.fromModelToDomain(any())).willReturn(expectedStep)

    val expectedGoal = aValidGoal(
      reference = updateGoalRequest.goalReference,
      title = updateGoalRequest.title,
      reviewDate = updateGoalRequest.reviewDate,
      status = GoalStatus.COMPLETED,
      notes = updateGoalRequest.notes,
      steps = mutableListOf(expectedStep),
      // JPA managed fields - expect these all to be null
      createdAt = null,
      createdBy = null,
      createdByDisplayName = null,
      lastUpdatedAt = null,
      lastUpdatedBy = null,
      lastUpdatedByDisplayName = null,
    )

    // When
    val actual = mapper.fromModelToDomain(updateGoalRequest)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expectedGoal)
    verify(stepMapper).fromModelToDomain(updateStepRequest)
  }

  @Test
  fun `should map from domain to model`() {
    // Given
    val step = aValidStep()
    val goal = aValidGoal(
      status = GoalStatus.ACTIVE,
      reviewDate = null,
      steps = mutableListOf(step),
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      lastUpdatedBy = "bjones_gen",
      lastUpdatedByDisplayName = "Barry Jones",
    )
    val expectedStepResponse = aValidStepResponse()
    val expectedDateTime = OffsetDateTime.now()
    given(stepMapper.fromDomainToModel(any())).willReturn(expectedStepResponse)
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)
    val expected = aValidGoalResponse(
      reference = goal.reference,
      title = goal.title,
      reviewDate = null,
      status = GoalStatusApi.ACTIVE,
      notes = goal.notes,
      steps = listOf(expectedStepResponse),
      createdAt = expectedDateTime,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      updatedAt = expectedDateTime,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )

    // When
    val actual = mapper.fromDomainToModel(goal)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(stepMapper).fromDomainToModel(step)
  }
}
