package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service.NoteService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanService
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.GoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.QualificationsResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidGoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.note.aValidNoteResponse
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class SubjectAccessRequestServiceTest {

  @InjectMocks
  private lateinit var subjectAccessRequestService: SubjectAccessRequestService

  @Mock
  private lateinit var inductionService: InductionService

  @Mock
  private lateinit var actionPlanService: ActionPlanService

  @Mock
  private lateinit var noteService: NoteService

  @Mock
  private lateinit var inductionMapper: InductionResourceMapper

  @Mock
  private lateinit var goalMapper: GoalResourceMapper

  @Mock
  private lateinit var qualificationsResourceMapper: QualificationsResourceMapper

  @Test
  fun `should return induction and action plan data for a prisoner without date filtering`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val induction = aFullyPopulatedInduction(
      prisonNumber = prisonNumber,
      createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
    )
    given(inductionService.getInductionForPrisoner(any())).willReturn(induction)
    val expectedInductionResponse = aValidInductionResponse()
    given(inductionMapper.toInductionResponse(any())).willReturn(expectedInductionResponse)

    val goal1 = aValidGoal(title = "Goal 1", createdAt = Instant.parse("2024-01-01T10:00:00.000Z"))
    val goal2 = aValidGoal(title = "Goal 2", createdAt = Instant.parse("2024-02-15T10:00:00.000Z"))
    val actionPlan = aValidActionPlan(prisonNumber = prisonNumber, goals = listOf(goal1, goal2))
    given(actionPlanService.getActionPlan(any())).willReturn(actionPlan)

    val note1ForGoal1 = aValidNoteDto(content = "Note 1 for Goal 1", entityReference = goal1.reference, entityType = EntityType.GOAL)
    val note1ForGoal2 = aValidNoteDto(content = "Note 1 for Goal 2", entityReference = goal2.reference, entityType = EntityType.GOAL)
    val note2ForGoal2 = aValidNoteDto(content = "Note 2 for Goal 2", entityReference = goal2.reference, entityType = EntityType.GOAL)
    given(noteService.getNotes(goal1.reference, EntityType.GOAL)).willReturn(listOf(note1ForGoal1))
    given(noteService.getNotes(goal2.reference, EntityType.GOAL)).willReturn(listOf(note1ForGoal2, note2ForGoal2))

    val expectedGoalResponse1 = aValidGoalResponse(
      title = "Goal 1",
      goalNotes = listOf(aValidNoteResponse(content = "Note 1 for Goal 1")),
    )
    val expectedGoalResponse2 = aValidGoalResponse(
      title = "Goal 2",
      goalNotes = listOf(aValidNoteResponse(content = "Note 1 for Goal 2"), aValidNoteResponse(content = "Note 2 for Goal 2")),
    )
    given(goalMapper.fromDomainToModel(any(), any())).willReturn(expectedGoalResponse1, expectedGoalResponse2)

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonNumber, null, null)
    val sarContent = sarResponse!!.content as SubjectAccessRequestContent

    // Then
    verify(inductionService).getInductionForPrisoner(prisonNumber)
    verify(inductionMapper).toInductionResponse(induction)

    verify(actionPlanService).getActionPlan(prisonNumber)
    verify(goalMapper).fromDomainToModel(goal1, listOf(note1ForGoal1))
    verify(goalMapper).fromDomainToModel(goal2, listOf(note1ForGoal2, note2ForGoal2))

    verify(noteService).getNotes(goal1.reference, EntityType.GOAL)
    verify(noteService).getNotes(goal2.reference, EntityType.GOAL)

    with(sarContent) {
      assertThat(this.induction).isEqualTo(expectedInductionResponse)
      assertThat(goals).isEqualTo(setOf(expectedGoalResponse1, expectedGoalResponse2))
    }
  }

  @Test
  fun `should return induction and action plan data for a prisoner filtered by from date`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val induction = aFullyPopulatedInduction(
      prisonNumber = prisonNumber,
      createdAt = Instant.parse("2024-01-01T10:00:00.000Z"),
    )
    given(inductionService.getInductionForPrisoner(any())).willReturn(induction)

    val goal1 = aValidGoal(title = "Goal 1", createdAt = Instant.parse("2024-01-01T10:00:00.000Z"))
    val goal2 = aValidGoal(title = "Goal 2", createdAt = Instant.parse("2024-02-15T10:00:00.000Z"))
    val actionPlan = aValidActionPlan(prisonNumber = prisonNumber, goals = listOf(goal1, goal2))
    given(actionPlanService.getActionPlan(any())).willReturn(actionPlan)

    val note1ForGoal2 = aValidNoteDto(content = "Note 1 for Goal 2", entityReference = goal2.reference, entityType = EntityType.GOAL)
    given(noteService.getNotes(goal2.reference, EntityType.GOAL)).willReturn(listOf(note1ForGoal2))

    val expectedGoalResponse2 = aValidGoalResponse(
      title = "Goal 2",
      goalNotes = listOf(aValidNoteResponse(content = "Note 1 for Goal 2")),
    )
    given(goalMapper.fromDomainToModel(any(), any())).willReturn(expectedGoalResponse2)

    // When
    val fromDate = LocalDate.parse("2024-01-15")
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonNumber, fromDate, null)
    val sarContent = sarResponse!!.content as SubjectAccessRequestContent

    // Then
    verify(inductionService).getInductionForPrisoner(prisonNumber)
    verify(inductionMapper, never()).toInductionResponse(any())

    verify(actionPlanService).getActionPlan(prisonNumber)
    verify(goalMapper).fromDomainToModel(goal2, listOf(note1ForGoal2))

    verify(noteService).getNotes(goal2.reference, EntityType.GOAL)

    with(sarContent) {
      assertThat(this.induction).isNull()
      assertThat(goals).isEqualTo(setOf(expectedGoalResponse2))
    }
  }

  @Test
  fun `should return induction and action plan data for a prisoner filtered by to date`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val induction = aFullyPopulatedInduction(
      prisonNumber = prisonNumber,
      createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
    )
    val expectedInductionResponse = aValidInductionResponse()
    val expectedQualificationsResponse = aValidPreviousQualificationsResponse(
      educationLevel = EducationLevel.NOT_SURE,
    )
    given(inductionService.getInductionForPrisoner(any())).willReturn(induction)
    given(inductionService.getQualifications(any())).willReturn(induction.previousQualifications)
    given(inductionMapper.toInductionResponse(any())).willReturn(expectedInductionResponse)
    given(qualificationsResourceMapper.toPreviousQualificationsResponse(any())).willReturn(expectedQualificationsResponse)

    val goal1 = aValidGoal(createdAt = Instant.parse("2024-01-01T10:00:00.000Z"))
    val goal2 = aValidGoal(createdAt = Instant.parse("2024-02-15T10:00:00.000Z"))
    val actionPlan = aValidActionPlan(prisonNumber = prisonNumber, goals = listOf(goal1, goal2))
    given(actionPlanService.getActionPlan(any())).willReturn(actionPlan)

    given(noteService.getNotes(any(), any())).willReturn(emptyList())

    val expectedGoalResponse1 = aValidGoalResponse(goalNotes = emptyList())
    given(goalMapper.fromDomainToModel(any(), any())).willReturn(expectedGoalResponse1)

    // When
    val toDate = LocalDate.parse("2024-01-10")
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonNumber, null, toDate)
    val sarContent = sarResponse!!.content as SubjectAccessRequestContent

    // Then
    verify(inductionService).getInductionForPrisoner(prisonNumber)
    verify(inductionMapper).toInductionResponse(induction)

    verify(actionPlanService).getActionPlan(prisonNumber)
    verify(goalMapper).fromDomainToModel(goal1, emptyList())

    verify(noteService).getNotes(goal1.reference, EntityType.GOAL)

    with(sarContent) {
      assertThat(this.induction).isEqualTo(expectedInductionResponse)
      assertThat(goals).isEqualTo(setOf(expectedGoalResponse1))
      assertThat(previousQualifications).isEqualTo(expectedQualificationsResponse)
    }
  }

  @Test
  fun `should return induction and action plan data for a prisoner filtered by from date and to date`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val induction = aFullyPopulatedInduction(
      prisonNumber = prisonNumber,
      createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
    )
    given(inductionService.getInductionForPrisoner(any())).willReturn(induction)
    given(inductionService.getQualifications(any())).willReturn(induction.previousQualifications)

    val goal1 = aValidGoal(title = "Goal 1", createdAt = Instant.parse("2024-01-01T10:00:00.000Z"))
    val goal2 = aValidGoal(title = "Goal 2", createdAt = Instant.parse("2024-02-15T10:00:00.000Z"))
    val goal3 = aValidGoal(title = "Goal 3", createdAt = Instant.parse("2024-03-10T10:00:00.000Z"))
    val actionPlan = aValidActionPlan(prisonNumber = prisonNumber, goals = listOf(goal1, goal2, goal3))
    given(actionPlanService.getActionPlan(any())).willReturn(actionPlan)

    given(noteService.getNotes(any(), any())).willReturn(emptyList())

    val expectedGoalResponse2 = aValidGoalResponse(title = "Goal 2", goalNotes = emptyList())
    given(goalMapper.fromDomainToModel(any(), any())).willReturn(expectedGoalResponse2)

    // When
    val fromDate = LocalDate.parse("2024-02-01")
    val toDate = LocalDate.parse("2024-03-01")
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonNumber, fromDate, toDate)
    val sarContent = sarResponse!!.content as SubjectAccessRequestContent

    // Then
    verify(inductionService).getInductionForPrisoner(prisonNumber)
    verify(inductionService).getQualifications(prisonNumber)
    verify(inductionMapper, never()).toInductionResponse(any())

    verify(actionPlanService).getActionPlan(prisonNumber)
    verify(goalMapper).fromDomainToModel(goal2, emptyList())

    verify(noteService).getNotes(goal2.reference, EntityType.GOAL)

    with(sarContent) {
      assertThat(this.induction).isNull()
      assertThat(goals).isEqualTo(setOf(expectedGoalResponse2))
    }
  }

  @Test
  fun `should return null when no data is found`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    given(inductionService.getInductionForPrisoner(any())).willReturn(null)

    given(actionPlanService.getActionPlan(any())).willThrow(ActionPlanNotFoundException(prisonNumber))

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonNumber, null, null)

    // Then
    verify(inductionService).getInductionForPrisoner(prisonNumber)
    verify(inductionMapper, never()).toInductionResponse(any())

    verify(actionPlanService).getActionPlan(prisonNumber)
    verify(goalMapper, never()).fromDomainToModel(any(), any())

    verify(noteService, never()).getNotes(any(), any())

    assertThat(sarResponse).isNull()
  }
}
