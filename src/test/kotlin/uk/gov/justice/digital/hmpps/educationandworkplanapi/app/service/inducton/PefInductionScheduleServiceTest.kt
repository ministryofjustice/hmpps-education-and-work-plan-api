package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleDateCalculationService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PefInductionScheduleServiceTest {

  @InjectMocks
  private lateinit var inductionScheduleService: PefInductionScheduleService

  @Mock
  private lateinit var inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter

  @Mock
  private lateinit var inductionScheduleEventService: InductionScheduleEventService

  @Mock
  private lateinit var inductionScheduleDateCalculationService: InductionScheduleDateCalculationService

  @Test
  fun `should return updated deadline date for processing transfer`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber)

    val expected = LocalDate.now().plusDays(20)
    given(inductionScheduleDateCalculationService.calculateAdjustedInductionDueDate(any()))
      .willReturn(expected)

    // When
    val actual = inductionScheduleService.updatedInductionDeadlineForProcessingTransfer(inductionSchedule)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(inductionScheduleDateCalculationService).calculateAdjustedInductionDueDate(inductionSchedule)
  }

  @Test
  fun `should return updated status for processing transfer`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber)

    // When
    val actual = inductionScheduleService.updatedStatusForProcessingTransfer(inductionSchedule)

    // Then
    assertThat(actual).isEqualTo(InductionScheduleStatus.SCHEDULED)
  }
}
