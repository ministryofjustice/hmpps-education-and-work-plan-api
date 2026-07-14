package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleDateCalculationService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.EducationAssessmentEventRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PesInductionScheduleServiceTest {
  private val inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter = mock()

  private val inductionScheduleEventService: InductionScheduleEventService = mock()

  private val inductionScheduleDateCalculationService: InductionScheduleDateCalculationService = mock()

  private val educationAssessmentEventRepository: EducationAssessmentEventRepository = mock()

  private val fixedTimestamp = Instant.parse("2026-04-17T09:13:22.123Z")
  private val clock = Clock.fixed(fixedTimestamp, ZoneId.of("UTC"))

  private val inductionScheduleService = PesInductionScheduleService(
    inductionSchedulePersistenceAdapter,
    inductionScheduleEventService,
    inductionScheduleDateCalculationService,
    educationAssessmentEventRepository,
    clock,
  )

  @Test
  fun `should return updated deadline date for processing transfer given prisoner has completed screenings and assessments`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber)

    given(educationAssessmentEventRepository.existsByPrisonNumberAndStatus(any(), any()))
      .willReturn(true)

    val expected = LocalDate.now().plusDays(20)
    given(inductionScheduleDateCalculationService.calculateAdjustedInductionDueDate(any()))
      .willReturn(expected)

    // When
    val actual = inductionScheduleService.updatedInductionDeadlineForProcessingTransfer(inductionSchedule)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(inductionScheduleDateCalculationService).calculateAdjustedInductionDueDate(inductionSchedule)
    verify(educationAssessmentEventRepository).existsByPrisonNumberAndStatus(
      prisonNumber,
      EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
    )
  }

  @Test
  fun `should return updated deadline date for processing transfer given prisoner has not completed screenings and assessments`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber)

    given(educationAssessmentEventRepository.existsByPrisonNumberAndStatus(any(), any()))
      .willReturn(false)

    val expected = LocalDate.now(clock)

    // When
    val actual = inductionScheduleService.updatedInductionDeadlineForProcessingTransfer(inductionSchedule)

    // Then
    assertThat(actual).isEqualTo(expected)
    verifyNoInteractions(inductionScheduleDateCalculationService)
    verify(educationAssessmentEventRepository).existsByPrisonNumberAndStatus(
      prisonNumber,
      EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
    )
  }

  @Test
  fun `should return updated status for processing transfer given prisoner has completed screenings and assessments`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber)

    given(educationAssessmentEventRepository.existsByPrisonNumberAndStatus(any(), any()))
      .willReturn(true)

    // When
    val actual = inductionScheduleService.updatedStatusForProcessingTransfer(inductionSchedule)

    // Then
    assertThat(actual).isEqualTo(InductionScheduleStatus.SCHEDULED)
    verify(educationAssessmentEventRepository).existsByPrisonNumberAndStatus(
      prisonNumber,
      EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
    )
  }

  @Test
  fun `should return updated status for processing transfer given prisoner has not completed screenings and assessments`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber)

    given(educationAssessmentEventRepository.existsByPrisonNumberAndStatus(any(), any()))
      .willReturn(false)

    // When
    val actual = inductionScheduleService.updatedStatusForProcessingTransfer(inductionSchedule)

    // Then
    assertThat(actual).isEqualTo(InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
    verify(educationAssessmentEventRepository).existsByPrisonNumberAndStatus(
      prisonNumber,
      EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
    )
  }
}
