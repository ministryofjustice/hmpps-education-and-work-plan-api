package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidStep
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateStepDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.note.NoteResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidGoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidStepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidUpdateStepRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.note.aValidNoteResponse
import java.time.LocalDate
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus as GoalStatusApi

@ExtendWith(MockitoExtension::class)
internal class GoalResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: GoalResourceMapper

  @Mock
  private lateinit var stepMapper: StepResourceMapper

  @Mock
  private lateinit var noteResourceMapper: NoteResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

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
    )

    val expectedStep = aValidUpdateStepDto()
    given(stepMapper.fromModelToDto(any<UpdateStepRequest>())).willReturn(expectedStep)

    val expectedGoal = aValidUpdateGoalDto(
      reference = updateGoalRequest.goalReference,
      title = updateGoalRequest.title,
      prisonId = updateGoalRequest.prisonId,
      targetCompletionDate = updateGoalRequest.targetCompletionDate,
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
      targetCompletionDate = LocalDate.now().plusMonths(6),
      steps = mutableListOf(step),
      createdBy = "asmith_gen",
      createdAtPrison = "BXI",
      lastUpdatedBy = "bjones_gen",
      lastUpdatedAtPrison = "MDI",
    )

    val goalNote1 = aValidNoteDto(content = "Note 1", entityReference = goal.reference, entityType = EntityType.GOAL)
    val goalNote2 = aValidNoteDto(content = "Note 2", entityReference = goal.reference, entityType = EntityType.GOAL)
    val goalNotes = listOf(goalNote1, goalNote2)

    val expectedStepResponse = aValidStepResponse()
    val expectedDateTime = OffsetDateTime.now()
    given(stepMapper.fromDomainToModel(any())).willReturn(expectedStepResponse)
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    val expectedNote1 = aValidNoteResponse(content = "Note 1", type = NoteType.GOAL)
    val expectedNote2 = aValidNoteResponse(content = "Note 2", type = NoteType.GOAL)
    given(noteResourceMapper.fromDomainToModel(any())).willReturn(expectedNote1, expectedNote2)

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    val expected = aValidGoalResponse(
      reference = goal.reference,
      title = goal.title,
      targetCompletionDate = goal.targetCompletionDate,
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
      goalNotes = listOf(expectedNote1, expectedNote2),
    )

    // When
    val actual = mapper.fromDomainToModel(goal, goalNotes)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(stepMapper).fromDomainToModel(step)
    verify(noteResourceMapper).fromDomainToModel(goalNote1)
    verify(noteResourceMapper).fromDomainToModel(goalNote2)
    verify(userService).getUserDetails("asmith_gen")
    verify(userService).getUserDetails("bjones_gen")
  }
}
