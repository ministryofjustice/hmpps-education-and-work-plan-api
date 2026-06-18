package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.sar

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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidStep
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.StepResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidStepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.sar.aValidSarGoalResponse
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus as GoalStatusApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonToArchiveGoal as ReasonToArchiveGoalApi

@ExtendWith(MockitoExtension::class)
internal class SarGoalResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: SarGoalResourceMapper

  @Mock
  private lateinit var stepMapper: StepResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map from domain to model given an active goal`() {
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

    val goalNote1 = aValidNoteDto(
      content = "Note 1",
      entityReference = goal.reference,
      entityType = EntityType.GOAL,
      createdAt = Instant.now().minusSeconds(60),
    )
    val goalNote2 = aValidNoteDto(
      content = "Note 2",
      entityReference = goal.reference,
      entityType = EntityType.GOAL,
      createdAt = Instant.now(),
    )
    val goalNotes = listOf(goalNote1, goalNote2)

    val expectedStepResponse = aValidStepResponse()
    val expectedDateTime = OffsetDateTime.now()
    given(stepMapper.fromDomainToModel(any())).willReturn(expectedStepResponse)
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    val expected = aValidSarGoalResponse(
      title = goal.title,
      targetCompletionDate = goal.targetCompletionDate,
      status = GoalStatusApi.ACTIVE,
      goalNote = "Note 2",
      completionNote = null,
      archiveReason = null,
      archiveReasonOther = null,
      archiveNote = null,
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
    val actual = mapper.fromDomainToModel(goal, goalNotes)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(stepMapper).fromDomainToModel(step)
    verify(userService).getUserDetails("asmith_gen")
    verify(userService).getUserDetails("bjones_gen")
  }

  @Test
  fun `should map from domain to model given an archived goal`() {
    // Given
    val step = aValidStep()
    val goal = aValidGoal(
      status = GoalStatus.ARCHIVED,
      targetCompletionDate = LocalDate.now().plusMonths(6),
      steps = mutableListOf(step),
      archiveReason = ReasonToArchiveGoal.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL,
      createdBy = "asmith_gen",
      createdAtPrison = "BXI",
      lastUpdatedBy = "bjones_gen",
      lastUpdatedAtPrison = "MDI",
    )

    val goalNote1 = aValidNoteDto(
      content = "Note 1",
      entityReference = goal.reference,
      entityType = EntityType.GOAL,
      noteType = NoteType.GOAL,
      createdAt = Instant.now().minusSeconds(60),
    )
    val goalNote2 = aValidNoteDto(
      content = "Note 2",
      entityReference = goal.reference,
      entityType = EntityType.GOAL,
      noteType = NoteType.GOAL,
      createdAt = Instant.now(),
    )
    val archivedNote1 = aValidNoteDto(
      content = "Archive Note 1",
      entityReference = goal.reference,
      entityType = EntityType.GOAL,
      noteType = NoteType.GOAL_ARCHIVAL,
      createdAt = Instant.now().minusSeconds(30),
    )
    val archivedNote2 = aValidNoteDto(
      content = "Archive Note 2",
      entityReference = goal.reference,
      entityType = EntityType.GOAL,
      noteType = NoteType.GOAL_ARCHIVAL,
      createdAt = Instant.now(),
    )
    val goalNotes = listOf(goalNote1, goalNote2, archivedNote1, archivedNote2)

    val expectedStepResponse = aValidStepResponse()
    val expectedDateTime = OffsetDateTime.now()
    given(stepMapper.fromDomainToModel(any())).willReturn(expectedStepResponse)
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    val expected = aValidSarGoalResponse(
      title = goal.title,
      targetCompletionDate = goal.targetCompletionDate,
      status = GoalStatusApi.ARCHIVED,
      goalNote = "Note 2",
      completionNote = null,
      archiveReason = ReasonToArchiveGoalApi.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL,
      archiveReasonOther = null,
      archiveNote = "Archive Note 2",
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
    val actual = mapper.fromDomainToModel(goal, goalNotes)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(stepMapper).fromDomainToModel(step)
    verify(userService).getUserDetails("asmith_gen")
    verify(userService).getUserDetails("bjones_gen")
  }

  @Test
  fun `should map from domain to model given a completed goal`() {
    // Given
    val step = aValidStep()
    val goal = aValidGoal(
      status = GoalStatus.COMPLETED,
      targetCompletionDate = LocalDate.now().plusMonths(6),
      steps = mutableListOf(step),
      createdBy = "asmith_gen",
      createdAtPrison = "BXI",
      lastUpdatedBy = "bjones_gen",
      lastUpdatedAtPrison = "MDI",
    )

    val goalNote1 = aValidNoteDto(
      content = "Note 1",
      entityReference = goal.reference,
      entityType = EntityType.GOAL,
      noteType = NoteType.GOAL,
      createdAt = Instant.now().minusSeconds(60),
    )
    val goalNote2 = aValidNoteDto(
      content = "Note 2",
      entityReference = goal.reference,
      entityType = EntityType.GOAL,
      noteType = NoteType.GOAL,
      createdAt = Instant.now(),
    )
    val completionNote = aValidNoteDto(
      content = "Completion Note 1",
      entityReference = goal.reference,
      entityType = EntityType.GOAL,
      noteType = NoteType.GOAL_COMPLETION,
    )
    val goalNotes = listOf(goalNote1, goalNote2, completionNote)

    val expectedStepResponse = aValidStepResponse()
    val expectedDateTime = OffsetDateTime.now()
    given(stepMapper.fromDomainToModel(any())).willReturn(expectedStepResponse)
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    val expected = aValidSarGoalResponse(
      title = goal.title,
      targetCompletionDate = goal.targetCompletionDate,
      status = GoalStatusApi.COMPLETED,
      goalNote = "Note 2",
      completionNote = "Completion Note 1",
      archiveReason = null,
      archiveReasonOther = null,
      archiveNote = null,
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
    val actual = mapper.fromDomainToModel(goal, goalNotes)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(stepMapper).fromDomainToModel(step)
    verify(userService).getUserDetails("asmith_gen")
    verify(userService).getUserDetails("bjones_gen")
  }
}
