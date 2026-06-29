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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service.EducationService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.aValidEmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.service.EmployabilitySkillsService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service.NoteService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidCompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanService
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.education.EducationResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionHistoryScheduleResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CompletedActionPlanReviewResponseMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.ReviewScheduleHistoryResponseMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.sar.SarGoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidEducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidEmployabilitySkillResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidInductionResponseForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidInductionScheduleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidReviewScheduleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidCompletedActionPlanReviewResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.sar.aValidSarGoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.sar.assertThat
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
  private lateinit var employabilitySkillsService: EmployabilitySkillsService

  @Mock
  private lateinit var actionPlanService: ActionPlanService

  @Mock
  private lateinit var noteService: NoteService

  @Mock
  private lateinit var inductionMapper: InductionResourceMapper

  @Mock
  private lateinit var sarGoalMapper: SarGoalResourceMapper

  @Mock
  private lateinit var educationService: EducationService

  @Mock
  private lateinit var educationResourceMapper: EducationResourceMapper

  @Mock
  private lateinit var reviewService: ReviewService

  @Mock
  private lateinit var completedActionPlanReviewResponseMapper: CompletedActionPlanReviewResponseMapper

  @Mock
  private lateinit var inductionScheduleService: InductionScheduleService

  @Mock
  private lateinit var inductionScheduleMapper: InductionHistoryScheduleResourceMapper

  @Mock
  private lateinit var reviewScheduleService: ReviewScheduleService

  @Mock
  private lateinit var reviewScheduleMapper: ReviewScheduleHistoryResponseMapper

  @Test
  fun `should return SAR content data for a prisoner without date filtering`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val induction = aFullyPopulatedInduction(
      prisonNumber = prisonNumber,
      createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
    )
    given(inductionService.getInductionForPrisoner(any())).willReturn(induction)
    given(educationService.getPreviousQualificationsForPrisoner(any())).willReturn(induction.previousQualifications)

    val employabilitySkills = listOf(aValidEmployabilitySkill())
    given(employabilitySkillsService.getEmployabilitySkills(any())).willReturn(employabilitySkills)

    val expectedEmployabilitySkills = listOf(aValidEmployabilitySkillResponse())
    val expectedInductionResponse = aValidInductionResponseForPrisonerNotLookingToWork(
      employabilitySkills = expectedEmployabilitySkills,
    )
    given(inductionMapper.toInductionResponse(any(), any())).willReturn(expectedInductionResponse)

    val goal1 = aValidGoal(title = "Goal 1", createdAt = Instant.parse("2024-01-01T10:00:00.000Z"))
    val goal2 = aValidGoal(title = "Goal 2", createdAt = Instant.parse("2024-02-15T10:00:00.000Z"))
    val actionPlan = aValidActionPlan(prisonNumber = prisonNumber, goals = listOf(goal1, goal2))
    given(actionPlanService.getActionPlan(any())).willReturn(actionPlan)

    val note1ForGoal1 = aValidNoteDto(content = "Note 1 for Goal 1", entityReference = goal1.reference, entityType = EntityType.GOAL)
    val note1ForGoal2 = aValidNoteDto(content = "Note 1 for Goal 2", entityReference = goal2.reference, entityType = EntityType.GOAL)
    val note2ForGoal2 = aValidNoteDto(content = "Note 2 for Goal 2", entityReference = goal2.reference, entityType = EntityType.GOAL)
    given(noteService.getNotes(goal1.reference, EntityType.GOAL)).willReturn(listOf(note1ForGoal1))
    given(noteService.getNotes(goal2.reference, EntityType.GOAL)).willReturn(listOf(note1ForGoal2, note2ForGoal2))

    val expectedGoalResponse1 = aValidSarGoalResponse(
      title = "Goal 1",
      goalNote = "Note 1 for Goal 1",
    )
    val expectedGoalResponse2 = aValidSarGoalResponse(
      title = "Goal 2",
      goalNote = "Note 2 for Goal 2",
    )
    given(sarGoalMapper.fromDomainToModel(any(), any())).willReturn(expectedGoalResponse1, expectedGoalResponse2)

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonNumber, null, null)
    val sarContent = sarResponse!!.content as SubjectAccessRequestContent

    // Then
    assertThat(sarContent)
      .induction { it.isEqualTo(expectedInductionResponse) }
      .hasNumberOfGoals(2)
      .goal(1) { it.isEqualTo(expectedGoalResponse1) }
      .goal(2) { it.isEqualTo(expectedGoalResponse2) }

    verify(inductionService).getInductionForPrisoner(prisonNumber)
    verify(employabilitySkillsService).getEmployabilitySkills(prisonNumber)
    verify(inductionMapper).toInductionResponse(induction, employabilitySkills)

    verify(actionPlanService).getActionPlan(prisonNumber)
    verify(sarGoalMapper).fromDomainToModel(goal1, listOf(note1ForGoal1))
    verify(sarGoalMapper).fromDomainToModel(goal2, listOf(note1ForGoal2, note2ForGoal2))

    verify(noteService).getNotes(goal1.reference, EntityType.GOAL)
    verify(noteService).getNotes(goal2.reference, EntityType.GOAL)
  }

  @Test
  fun `should return SAR content for a prisoner filtered by from date`() {
    // Given
    val fromDate = LocalDate.parse("2024-01-15")

    val prisonNumber = randomValidPrisonNumber()

    val induction = aFullyPopulatedInduction(
      prisonNumber = prisonNumber,
      createdAt = Instant.parse("2024-01-01T10:00:00.000Z"),
      previousQualifications = aValidPreviousQualifications(
        createdAt = Instant.parse("2024-01-01T10:00:00.000Z"),
      ),
    )
    given(inductionService.getInductionForPrisoner(any())).willReturn(induction)
    given(educationService.getPreviousQualificationsForPrisoner(any())).willReturn(induction.previousQualifications)

    val employabilitySkills = listOf(
      aValidEmployabilitySkill(
        createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
      ),
    )
    given(employabilitySkillsService.getEmployabilitySkills(any())).willReturn(employabilitySkills)

    val completedReview = aValidCompletedReview(
      createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
    )
    given(reviewService.getCompletedReviewsForPrisoner(any())).willReturn(listOf(completedReview))

    val goal1 = aValidGoal(title = "Goal 1", createdAt = Instant.parse("2024-01-01T10:00:00.000Z"))
    val goal2 = aValidGoal(title = "Goal 2", createdAt = Instant.parse("2024-02-15T10:00:00.000Z"))
    val actionPlan = aValidActionPlan(prisonNumber = prisonNumber, goals = listOf(goal1, goal2))
    given(actionPlanService.getActionPlan(any())).willReturn(actionPlan)

    val note1ForGoal2 = aValidNoteDto(content = "Note 1 for Goal 2", entityReference = goal2.reference, entityType = EntityType.GOAL)
    given(noteService.getNotes(goal2.reference, EntityType.GOAL)).willReturn(listOf(note1ForGoal2))

    val expectedGoalResponse2 = aValidSarGoalResponse(
      title = "Goal 2",
      goalNote = "Note 1 for Goal 2",
    )
    given(sarGoalMapper.fromDomainToModel(any(), any())).willReturn(expectedGoalResponse2)

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonNumber, fromDate, null)
    val sarContent = sarResponse!!.content as SubjectAccessRequestContent

    // Then
    assertThat(sarContent)
      .hasNoInduction() // Expect the Induction to be null because it was created before the "fromDate"
      .hasNumberOfGoals(1)
      .goal(1) { it.isEqualTo(expectedGoalResponse2) }

    verify(inductionService).getInductionForPrisoner(prisonNumber)
    verify(inductionMapper, never()).toInductionResponse(any(), any())

    verify(actionPlanService).getActionPlan(prisonNumber)
    verify(sarGoalMapper).fromDomainToModel(goal2, listOf(note1ForGoal2))

    verify(noteService).getNotes(goal2.reference, EntityType.GOAL)

    verify(inductionScheduleService).getInductionScheduleHistoryForPrisoner(prisonNumber)
  }

  @Test
  fun `should return SAR content for a prisoner filtered by to date`() {
    // Given
    val toDate = LocalDate.parse("2024-01-10")

    val prisonNumber = randomValidPrisonNumber()

    val induction = aFullyPopulatedInduction(
      prisonNumber = prisonNumber,
      createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
      previousQualifications = aValidPreviousQualifications(
        createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
      ),
    )
    given(inductionService.getInductionForPrisoner(any())).willReturn(induction)
    given(educationService.getPreviousQualificationsForPrisoner(any())).willReturn(induction.previousQualifications)

    val employabilitySkills = listOf(
      aValidEmployabilitySkill(
        createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
      ),
    )
    given(employabilitySkillsService.getEmployabilitySkills(any())).willReturn(employabilitySkills)

    val completedReview = aValidCompletedReview(
      createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
    )
    given(reviewService.getCompletedReviewsForPrisoner(any())).willReturn(listOf(completedReview))

    val goal1 = aValidGoal(createdAt = Instant.parse("2024-01-01T10:00:00.000Z"))
    val goal2 = aValidGoal(createdAt = Instant.parse("2024-02-15T10:00:00.000Z"))
    val actionPlan = aValidActionPlan(prisonNumber = prisonNumber, goals = listOf(goal1, goal2))
    given(actionPlanService.getActionPlan(any())).willReturn(actionPlan)

    given(noteService.getNotes(any(), any())).willReturn(emptyList())

    val expectedInductionResponse = aValidInductionResponse()
    val expectedEducationResponse = aValidEducationResponse(
      educationLevel = EducationLevel.NOT_SURE,
    )
    val expectedCompletedActionPlanReviewResponse = aValidCompletedActionPlanReviewResponse()
    val expectedInductionScheduleResponse = aValidInductionScheduleResponse()
    val expectedReviewScheduleResponse = aValidReviewScheduleResponse()

    given(inductionMapper.toInductionResponse(any(), any())).willReturn(expectedInductionResponse)
    given(educationResourceMapper.toEducationResponse(any())).willReturn(expectedEducationResponse)
    given(completedActionPlanReviewResponseMapper.fromDomainToModel(any())).willReturn(expectedCompletedActionPlanReviewResponse)
    given(inductionScheduleMapper.toInductionResponse(any(), any())).willReturn(expectedInductionScheduleResponse)
    given(reviewScheduleMapper.fromDomainToModel(any())).willReturn(expectedReviewScheduleResponse)

    val expectedGoalResponse1 = aValidSarGoalResponse(goalNote = null)
    given(sarGoalMapper.fromDomainToModel(any(), any())).willReturn(expectedGoalResponse1)

    val inductionScheduleHistory = aValidInductionScheduleHistory(
      createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
    )
    given(inductionScheduleService.getInductionScheduleHistoryForPrisoner(prisonNumber)).willReturn(
      listOf(
        inductionScheduleHistory,
      ),
    )

    val reviewScheduleHistory = aValidReviewScheduleHistory(
      createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
    )
    given(reviewScheduleService.getReviewSchedulesForPrisoner(prisonNumber)).willReturn(
      listOf(
        reviewScheduleHistory,
      ),
    )

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonNumber, null, toDate)
    val sarContent = sarResponse!!.content as SubjectAccessRequestContent

    // Then
    assertThat(sarContent)
      .induction { it.isEqualTo(expectedInductionResponse) }
      .hasNumberOfGoals(1)
      .goal(1) { it.isEqualTo(expectedGoalResponse1) }
      .education { it.isEqualTo(expectedEducationResponse) }
      .hasNumberOfInductionScheduleRecords(1)
      .hasNumberOfReviewScheduleRecords(1)
      .hasNumberOfCompletedReviews(1)

    verify(inductionService).getInductionForPrisoner(prisonNumber)
    verify(employabilitySkillsService).getEmployabilitySkills(prisonNumber)

    verify(inductionMapper).toInductionResponse(induction, employabilitySkills)

    verify(actionPlanService).getActionPlan(prisonNumber)
    verify(sarGoalMapper).fromDomainToModel(goal1, emptyList())

    verify(noteService).getNotes(goal1.reference, EntityType.GOAL)
  }

  @Test
  fun `should return induction and action plan data for a prisoner filtered by from date and to date`() {
    // Given
    val fromDate = LocalDate.parse("2024-02-01")
    val toDate = LocalDate.parse("2024-03-01")

    val prisonNumber = randomValidPrisonNumber()

    val induction = aFullyPopulatedInduction(
      prisonNumber = prisonNumber,
      createdAt = LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC),
    )
    given(inductionService.getInductionForPrisoner(any())).willReturn(induction)
    given(educationService.getPreviousQualificationsForPrisoner(any())).willReturn(induction.previousQualifications)

    val goal1 = aValidGoal(title = "Goal 1", createdAt = Instant.parse("2024-01-01T10:00:00.000Z"))
    val goal2 = aValidGoal(title = "Goal 2", createdAt = Instant.parse("2024-02-15T10:00:00.000Z"))
    val goal3 = aValidGoal(title = "Goal 3", createdAt = Instant.parse("2024-03-10T10:00:00.000Z"))
    val actionPlan = aValidActionPlan(prisonNumber = prisonNumber, goals = listOf(goal1, goal2, goal3))
    given(actionPlanService.getActionPlan(any())).willReturn(actionPlan)

    given(noteService.getNotes(any(), any())).willReturn(emptyList())

    val expectedGoalResponse2 = aValidSarGoalResponse(title = "Goal 2", goalNote = null)
    given(sarGoalMapper.fromDomainToModel(any(), any())).willReturn(expectedGoalResponse2)

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonNumber, fromDate, toDate)
    val sarContent = sarResponse!!.content as SubjectAccessRequestContent

    // Then
    assertThat(sarContent)
      .hasNoInduction()
      .hasNumberOfGoals(1)
      .goal(1) { it.isEqualTo(expectedGoalResponse2) }

    verify(inductionService).getInductionForPrisoner(prisonNumber)
    verify(educationService).getPreviousQualificationsForPrisoner(prisonNumber)
    verify(employabilitySkillsService).getEmployabilitySkills(prisonNumber)
    verify(inductionMapper, never()).toInductionResponse(any(), any())

    verify(actionPlanService).getActionPlan(prisonNumber)
    verify(sarGoalMapper).fromDomainToModel(goal2, emptyList())

    verify(noteService).getNotes(goal2.reference, EntityType.GOAL)
  }

  @Test
  fun `should return null when no data is found`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    given(inductionService.getInductionForPrisoner(any())).willThrow(InductionNotFoundException(prisonNumber))
    given(actionPlanService.getActionPlan(any())).willThrow(ActionPlanNotFoundException(prisonNumber))
    given(educationService.getPreviousQualificationsForPrisoner(any())).willThrow(EducationNotFoundException(prisonNumber))
    given(inductionScheduleService.getInductionScheduleHistoryForPrisoner(any())).willReturn(emptyList())
    given(reviewScheduleService.getReviewSchedulesForPrisoner(any())).willReturn(emptyList())
    given(reviewService.getCompletedReviewsForPrisoner(any())).willReturn(emptyList())
    given(employabilitySkillsService.getEmployabilitySkills(any())).willReturn(emptyList())

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonNumber, null, null)

    // Then
    assertThat(sarResponse).isNull()

    verify(inductionService).getInductionForPrisoner(prisonNumber)
    verify(inductionMapper, never()).toInductionResponse(any(), any())
    verify(actionPlanService).getActionPlan(prisonNumber)
    verify(sarGoalMapper, never()).fromDomainToModel(any(), any())
    verify(educationService).getPreviousQualificationsForPrisoner(prisonNumber)
    verify(educationResourceMapper, never()).toEducationResponse(any())
    verify(inductionScheduleService).getInductionScheduleHistoryForPrisoner(prisonNumber)
    verify(inductionScheduleMapper, never()).toInductionResponse(any(), any())
    verify(reviewScheduleService).getReviewSchedulesForPrisoner(prisonNumber)
    verify(reviewScheduleMapper, never()).fromDomainToModel(any())
    verify(reviewService).getCompletedReviewsForPrisoner(prisonNumber)
    verify(completedActionPlanReviewResponseMapper, never()).fromDomainToModel(any())
    verify(employabilitySkillsService).getEmployabilitySkills(prisonNumber)
    verify(noteService, never()).getNotes(any(), any())
  }
}
