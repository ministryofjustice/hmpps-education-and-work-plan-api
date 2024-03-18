package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.GoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidGoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidInductionResponse
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
  private lateinit var inductionMapper: InductionResourceMapper

  @Mock
  private lateinit var goalMapper: GoalResourceMapper

  @Test
  fun `should return induction and action plan data for a prisoner without date filtering`() {
    // Given
    val prisonerNumber = aValidPrisonNumber()

    val inductionCreateTime = LocalDateTime.parse("2024-01-01T10:00:00")

    val goalCreateTimes = listOf(
      LocalDateTime.parse("2024-01-01T10:00:00"),
      LocalDateTime.parse("2024-02-15T10:00:00"),
    )

    setupMocks(prisonerNumber, inductionCreateTime, goalCreateTimes)
    given(inductionMapper.toInductionResponse(any())).willReturn(aValidInductionResponse())
    given(goalMapper.fromDomainToModel(any())).willReturn(aValidGoalResponse())

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonerNumber, null, null)
    val sarContent = sarResponse!!.content as SubjectAccessRequestService.SubjectAccessRequestContent

    // Then
    val inductionCaptor = argumentCaptor<Induction>()
    verify(inductionMapper, times(1)).toInductionResponse(inductionCaptor.capture())
    assertThat(inductionCaptor.firstValue.prisonNumber).isEqualTo(prisonerNumber)

    verify(goalMapper, times(2)).fromDomainToModel(any())

    with(sarContent) {
      assertThat(induction).isNotNull
      assertThat(goals).hasSize(2)
    }
  }

  @Test
  fun `should return induction and action plan data for a prisoner filtered by from date`() {
    // Given
    val prisonerNumber = aValidPrisonNumber()

    val inductionCreateTime = LocalDateTime.parse("2024-01-01T10:00:00")

    val goalCreateTimes = listOf(
      LocalDateTime.parse("2024-01-01T10:00:00"),
      LocalDateTime.parse("2024-02-15T10:00:00"),
    )

    setupMocks(prisonerNumber, inductionCreateTime, goalCreateTimes)
    given(goalMapper.fromDomainToModel(any())).willReturn(aValidGoalResponse())

    // When
    val fromDate = LocalDate.parse("2024-01-15")
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonerNumber, fromDate, null)
    val sarContent = sarResponse!!.content as SubjectAccessRequestService.SubjectAccessRequestContent

    // Then
    val inductionCaptor = argumentCaptor<Induction>()
    verify(inductionMapper, never()).toInductionResponse(inductionCaptor.capture())

    val goalsCaptor = argumentCaptor<Goal>()
    verify(goalMapper, times(1)).fromDomainToModel(goalsCaptor.capture())

    assertThat(goalsCaptor.firstValue.createdAt).isEqualTo(LocalDateTime.parse("2024-02-15T10:00:00").toInstant(ZoneOffset.UTC))

    with(sarContent) {
      assertThat(induction).isNull()
      assertThat(goals).hasSize(1)
    }
  }

  @Test
  fun `should return induction and action plan data for a prisoner filtered by to date`() {
    // Given
    val prisonerNumber = aValidPrisonNumber()

    val inductionCreateTime = LocalDateTime.parse("2024-01-01T10:00:00")

    val goalCreateTimes = listOf(
      LocalDateTime.parse("2024-01-01T10:00:00"),
      LocalDateTime.parse("2024-02-15T10:00:00"),
    )

    setupMocks(prisonerNumber, inductionCreateTime, goalCreateTimes)
    given(inductionMapper.toInductionResponse(any())).willReturn(aValidInductionResponse())
    given(goalMapper.fromDomainToModel(any())).willReturn(aValidGoalResponse())

    // When
    val toDate = LocalDate.parse("2024-01-10")
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonerNumber, null, toDate)
    val sarContent = sarResponse!!.content as SubjectAccessRequestService.SubjectAccessRequestContent

    // Then
    val inductionCaptor = argumentCaptor<Induction>()
    verify(inductionMapper, times(1)).toInductionResponse(inductionCaptor.capture())
    assertThat(inductionCaptor.firstValue.prisonNumber).isEqualTo(prisonerNumber)

    val goalsCaptor = argumentCaptor<Goal>()
    verify(goalMapper, times(1)).fromDomainToModel(goalsCaptor.capture())
    assertThat(goalsCaptor.firstValue.createdAt).isEqualTo(LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC))

    with(sarContent) {
      assertThat(induction).isNotNull
      assertThat(goals).hasSize(1)
    }
  }

  @Test
  fun `should return induction and action plan data for a prisoner filtered by from date and to date`() {
    // Given
    val prisoner = aValidPrisonNumber()

    val inductionCreateTime = LocalDateTime.parse("2024-01-01T10:00:00")

    val goalCreateTimes = listOf(
      LocalDateTime.parse("2024-01-01T10:00:00"),
      LocalDateTime.parse("2024-02-15T10:00:00"),
      LocalDateTime.parse("2024-03-10T10:00:00"),
    )

    setupMocks(prisoner, inductionCreateTime, goalCreateTimes)

    // When
    val fromDate = LocalDate.parse("2024-02-01")
    val toDate = LocalDate.parse("2024-03-01")
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisoner, fromDate, toDate)
    val sarContent = sarResponse!!.content as SubjectAccessRequestService.SubjectAccessRequestContent

    // Then
    verify(inductionMapper, never()).toInductionResponse(any())

    val goalsCaptor = argumentCaptor<Goal>()
    verify(goalMapper, times(1)).fromDomainToModel(goalsCaptor.capture())
    assertThat(goalsCaptor.firstValue.createdAt).isEqualTo(LocalDateTime.parse("2024-02-15T10:00:00").toInstant(ZoneOffset.UTC))

    with(sarContent) {
      assertThat(induction).isNull()
      assertThat(goals).hasSize(1)
    }
  }

  @Test
  fun `should return null when no data is found`() {
    // Given
    val prisonerNumber = aValidPrisonNumber()
    given(inductionService.getInductionForPrisoner(prisonerNumber)).willReturn(null)
    val emptyActionPlan = aValidActionPlan(prisonNumber = prisonerNumber, goals = emptyList())
    given(actionPlanService.getActionPlan(prisonerNumber)).willReturn(emptyActionPlan)

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prisonerNumber, null, null)

    // Then
    val inductionCaptor = argumentCaptor<Induction>()
    verify(inductionMapper, never()).toInductionResponse(inductionCaptor.capture())

    val goalsCaptor = argumentCaptor<Goal>()
    verify(goalMapper, never()).fromDomainToModel(goalsCaptor.capture())

    assertThat(sarResponse).isNull()
  }

  private fun setupMocks(prisonerNumber: String, inductionCreateTime: LocalDateTime, goalCreateTimes: List<LocalDateTime>) {
    val induction = aFullyPopulatedInduction(
      prisonNumber = prisonerNumber,
      createdAt = inductionCreateTime.toInstant(ZoneOffset.UTC),
    )
    given(inductionService.getInductionForPrisoner(prisonerNumber)).willReturn(induction)

    val goals = goalCreateTimes.map {
      aValidGoal(
        createdAt = it.toInstant(ZoneOffset.UTC),
      )
    }
    val actionPlan = aValidActionPlan(prisonNumber = prisonerNumber, goals = goals)
    given(actionPlanService.getActionPlan(prisonerNumber)).willReturn(actionPlan)
  }
}
