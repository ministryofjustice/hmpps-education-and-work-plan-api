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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidUpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidUpdateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateStepRequest
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
  fun `should map from CreateGoalRequest model to DTO`() {
    // Given
    val createStepRequest = aValidCreateStepRequest()
    val createGoalRequest = aValidCreateGoalRequest(
      targetCompletionDate = LocalDate.now(),
      steps = mutableListOf(createStepRequest),
    )

    val expectedStep = aValidCreateStepDto()
    given(stepMapper.fromModelToDto(any<CreateStepRequest>())).willReturn(expectedStep)

    val expectedGoal = aValidCreateGoalDto(
      title = createGoalRequest.title,
      prisonId = createGoalRequest.prisonId,
      targetCompletionDate = createGoalRequest.targetCompletionDate,
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
  fun `should map from UpdateGoalRequest model to DTO`() {
    // Given
    val updateStepRequest = aValidUpdateStepRequest()
    val updateGoalRequest = aValidUpdateGoalRequest(
      targetCompletionDate = LocalDate.now(),
      steps = mutableListOf(updateStepRequest),
      status = GoalStatusApi.ACTIVE,
    )

    val expectedStep = aValidUpdateStepDto()
    given(stepMapper.fromModelToDto(any<UpdateStepRequest>())).willReturn(expectedStep)

    val expectedGoal = aValidUpdateGoalDto(
      reference = updateGoalRequest.goalReference,
      title = updateGoalRequest.title,
      prisonId = updateGoalRequest.prisonId,
      targetCompletionDate = updateGoalRequest.targetCompletionDate,
      status = GoalStatus.ACTIVE,
      notes = updateGoalRequest.notes,
      steps = mutableListOf(expectedStep),
    )

    // When
    val actual = mapper.fromModelToDto(updateGoalRequest)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expectedGoal)
    verify(stepMapper).fromModelToDto(updateStepRequest)
  }

  @Test
  fun `should map from domain to model`() {
    // Given
    val step = aValidStep()
    val goal = aValidGoal(
      status = GoalStatus.ACTIVE,
      targetCompletionDate = null,
      steps = mutableListOf(step),
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      createdAtPrison = "BXI",
      lastUpdatedBy = "bjones_gen",
      lastUpdatedByDisplayName = "Barry Jones",
      lastUpdatedAtPrison = "MDI",
    )
    val expectedStepResponse = aValidStepResponse()
    val expectedDateTime = OffsetDateTime.now()
    given(stepMapper.fromDomainToModel(any())).willReturn(expectedStepResponse)
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)
    val expected = aValidGoalResponse(
      reference = goal.reference,
      title = goal.title,
      targetCompletionDate = null,
      status = GoalStatusApi.ACTIVE,
      notes = goal.notes,
      steps = listOf(expectedStepResponse),
      createdAt = expectedDateTime,
      createdAtPrison = "BXI",
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      updatedAt = expectedDateTime,
      updatedAtPrison = "MDI",
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
