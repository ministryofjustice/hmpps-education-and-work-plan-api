package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionService
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

  @Test
  fun `should return induction and action plan data for a prisoner without date filtering`() {
    // Given
    val prn = aValidPrisonNumber()

    val inductionCreateTime = LocalDateTime.parse("2024-01-01T10:00:00")

    val goalCreateTimes = listOf(
      LocalDateTime.parse("2024-01-01T10:00:00"),
      LocalDateTime.parse("2024-02-15T10:00:00"),
    )

    setupMocks(prn, inductionCreateTime, goalCreateTimes)

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prn, null, null)
    val sarContent = sarResponse!!.content as SubjectAccessRequestService.SubjectAccessRequestContent

    // Then
    with(sarContent) {
      assertThat(induction).isNotNull
      assertThat(induction!!.prisonNumber).isEqualTo(prn)
      assertThat(goals).hasSize(2)
    }
  }

  @Test
  fun `should return induction and action plan data for a prisoner filtered by from date`() {
    // Given
    val prn = aValidPrisonNumber()

    val inductionCreateTime = LocalDateTime.parse("2024-01-01T10:00:00")

    val goalCreateTimes = listOf(
      LocalDateTime.parse("2024-01-01T10:00:00"),
      LocalDateTime.parse("2024-02-15T10:00:00"),
    )

    setupMocks(prn, inductionCreateTime, goalCreateTimes)

    // When
    val fromDate = LocalDate.parse("2024-01-15")
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prn, fromDate, null)
    val sarContent = sarResponse!!.content as SubjectAccessRequestService.SubjectAccessRequestContent

    // Then
    with(sarContent) {
      assertThat(induction).isNull()
      assertThat(goals).hasSize(1)
      assertThat(goals.first().createdAt).isEqualTo(LocalDateTime.parse("2024-02-15T10:00:00").toInstant(ZoneOffset.UTC))
    }
  }

  @Test
  fun `should return induction and action plan data for a prisoner filtered by to date`() {
    // Given
    val prn = aValidPrisonNumber()

    val inductionCreateTime = LocalDateTime.parse("2024-01-01T10:00:00")

    val goalCreateTimes = listOf(
      LocalDateTime.parse("2024-01-01T10:00:00"),
      LocalDateTime.parse("2024-02-15T10:00:00"),
    )

    setupMocks(prn, inductionCreateTime, goalCreateTimes)

    // When
    val toDate = LocalDate.parse("2024-01-10")
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prn, null, toDate)
    val sarContent = sarResponse!!.content as SubjectAccessRequestService.SubjectAccessRequestContent

    // Then
    with(sarContent) {
      assertThat(induction).isNotNull
      assertThat(goals).hasSize(1)
      assertThat(goals.first().createdAt).isEqualTo(LocalDateTime.parse("2024-01-01T10:00:00").toInstant(ZoneOffset.UTC))
    }
  }

  @Test
  fun `should return induction and action plan data for a prisoner filtered by from date and to date`() {
    // Given
    val prn = aValidPrisonNumber()

    val inductionCreateTime = LocalDateTime.parse("2024-01-01T10:00:00")

    val goalCreateTimes = listOf(
      LocalDateTime.parse("2024-01-01T10:00:00"),
      LocalDateTime.parse("2024-02-15T10:00:00"),
      LocalDateTime.parse("2024-03-10T10:00:00"),
    )

    setupMocks(prn, inductionCreateTime, goalCreateTimes)

    // When
    val fromDate = LocalDate.parse("2024-02-01")
    val toDate = LocalDate.parse("2024-03-01")
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prn, fromDate, toDate)
    val sarContent = sarResponse!!.content as SubjectAccessRequestService.SubjectAccessRequestContent

    // Then
    with(sarContent) {
      assertThat(induction).isNull()
      assertThat(goals).hasSize(1)
      assertThat(goals.first().createdAt).isEqualTo(LocalDateTime.parse("2024-02-15T10:00:00").toInstant(ZoneOffset.UTC))
    }
  }

  @Test
  fun `should return null when no data is found`() {
    // Given
    val prn = aValidPrisonNumber()
    given(inductionService.getInductionForPrisoner(prn)).willReturn(null)
    val emptyActionPlan = aValidActionPlan(prisonNumber = prn, goals = emptyList())
    given(actionPlanService.getActionPlan(prn)).willReturn(emptyActionPlan)

    // When
    val sarResponse = subjectAccessRequestService.getPrisonContentFor(prn, null, null)

    // Then
    assertThat(sarResponse).isNull()
  }

  private fun setupMocks(prn: String, inductionCreateTime: LocalDateTime, goalCreateTimes: List<LocalDateTime>) {
    val induction = aFullyPopulatedInduction(
      prisonNumber = prn,
      createdAt = inductionCreateTime.toInstant(ZoneOffset.UTC),
    )
    given(inductionService.getInductionForPrisoner(prn)).willReturn(induction)

    val goals = goalCreateTimes.map {
      aValidGoal(
        createdAt = it.toInstant(ZoneOffset.UTC),
      )
    }

    val actionPlan = aValidActionPlan(prisonNumber = prn, goals = goals)
    given(actionPlanService.getActionPlan(prn)).willReturn(actionPlan)
  }
}
