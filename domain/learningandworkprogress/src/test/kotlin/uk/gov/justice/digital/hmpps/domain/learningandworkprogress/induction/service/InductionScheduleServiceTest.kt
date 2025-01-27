package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.EXEMPT_PRISONER_FAILED_TO_ENGAGE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InvalidInductionScheduleStatusException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.UpdatedInductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionScheduleStatusDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit.MINUTES
import java.time.temporal.ChronoUnit.SECONDS
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class InductionScheduleServiceTest {

  @InjectMocks
  private lateinit var service: InductionScheduleService

  @Mock
  private lateinit var inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter

  @Mock
  private lateinit var inductionScheduleEventService: InductionScheduleEventService

  @Mock
  private lateinit var inductionScheduleDateCalculationService: InductionScheduleDateCalculationService

  companion object {
    private val PRISON_NUMBER = randomValidPrisonNumber()
    private val PRISON_ID = "BXI"
    private val TODAY = LocalDate.now()
    private val NOW = Instant.now()
  }

  @Nested
  inner class GetInductionSchedule {
    @Test
    fun `should get induction schedule for prisoner`() {
      // Given
      val expected = aValidInductionSchedule()
      given(inductionSchedulePersistenceAdapter.getInductionSchedule(any())).willReturn(expected)

      // When
      val actual = service.getInductionScheduleForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(inductionSchedulePersistenceAdapter).getInductionSchedule(PRISON_NUMBER)
    }

    @Test
    fun `should fail to get induction schedule for prisoner given induction schedule does not exist`() {
      // Given
      given(inductionSchedulePersistenceAdapter.getInductionSchedule(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(InductionScheduleNotFoundException::class.java) {
        service.getInductionScheduleForPrisoner(PRISON_NUMBER)
      }

      // Then
      assertThat(exception).hasMessage("Induction schedule not found for prisoner [$PRISON_NUMBER]")
      assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
      verify(inductionSchedulePersistenceAdapter).getInductionSchedule(PRISON_NUMBER)
    }
  }

  @Nested
  inner class GetReviewSchedulesForPrisoner {
    @Test
    fun `should get induction schedule history for prisoner, sorted by last updated`() {
      // Given
      val inductionScheduleReference = UUID.randomUUID()

      given(inductionSchedulePersistenceAdapter.getInductionScheduleHistory(any())).willReturn(
        listOf(
          aValidInductionScheduleHistory(
            reference = inductionScheduleReference,
            version = 2,
            lastUpdatedAt = NOW.minus(1, MINUTES),
          ),
          aValidInductionScheduleHistory(
            reference = inductionScheduleReference,
            version = 1,
            lastUpdatedAt = NOW.minus(10, MINUTES),
          ),
          aValidInductionScheduleHistory(
            reference = inductionScheduleReference,
            version = 3,
            lastUpdatedAt = NOW.minus(30, SECONDS),
          ),
        ),
      )

      val expected = listOf(
        "Version: 3, LastUpdatedAt: ${NOW.minus(30, SECONDS)}",
        "Version: 2, LastUpdatedAt: ${NOW.minus(1, MINUTES)}",
        "Version: 1, LastUpdatedAt: ${NOW.minus(10, MINUTES)}",
      )

      // When
      val actual = service.getInductionScheduleHistoryForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual.map { "Version: ${it.version}, LastUpdatedAt: ${it.lastUpdatedAt}" }).isEqualTo(expected)
      verify(inductionSchedulePersistenceAdapter).getInductionScheduleHistory(PRISON_NUMBER)
    }

    @Test
    fun `should get induction schedule history given prisoner has no induction schedule`() {
      // Given
      given(inductionSchedulePersistenceAdapter.getInductionScheduleHistory(any())).willReturn(emptyList())

      // When
      val actual = service.getInductionScheduleHistoryForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual).isEmpty()
      verify(inductionSchedulePersistenceAdapter).getInductionScheduleHistory(PRISON_NUMBER)
    }
  }

  @Nested
  inner class CreateInductionSchedule {
    @Test
    fun `should create induction schedule given prisoner does not have an induction schedule`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val admissionDate = LocalDate.now()
      val prisonId = "BXI"

      given(inductionSchedulePersistenceAdapter.getInductionSchedule(any())).willReturn(null)

      val createInductionScheduleDto = aValidCreateInductionScheduleDto(prisonNumber = prisonNumber)
      given(inductionScheduleDateCalculationService.determineCreateInductionScheduleDto(any(), any(), any(), any())).willReturn(createInductionScheduleDto)

      val expectedInductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber)
      given(inductionSchedulePersistenceAdapter.createInductionSchedule(any())).willReturn(expectedInductionSchedule)

      // When
      val actual = service.createInductionSchedule(prisonNumber, admissionDate, prisonId)

      // Then
      assertThat(actual).isEqualTo(expectedInductionSchedule)
      verify(inductionSchedulePersistenceAdapter).getInductionSchedule(prisonNumber)
      verify(inductionScheduleDateCalculationService).determineCreateInductionScheduleDto(prisonNumber, admissionDate, prisonId, true)
      verify(inductionSchedulePersistenceAdapter).createInductionSchedule(createInductionScheduleDto)
      verify(inductionScheduleEventService).inductionScheduleCreated(actual)
    }

    @Test
    fun `should not create induction schedule given prisoner already has an induction schedule`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val admissionDate = LocalDate.now()
      val prisonId = "BXI"

      val existingInductionSchedule = aValidInductionSchedule(
        prisonNumber = prisonNumber,
        scheduleStatus = SCHEDULED,
      )
      given(inductionSchedulePersistenceAdapter.getInductionSchedule(any())).willReturn(existingInductionSchedule)

      // When
      val exception = catchThrowableOfType(
        InductionScheduleAlreadyExistsException::class.java,
      ) { service.createInductionSchedule(prisonNumber, admissionDate, prisonId) }

      // Then
      assertThat(exception.inductionSchedule).isEqualTo(existingInductionSchedule)
      verify(inductionSchedulePersistenceAdapter).getInductionSchedule(prisonNumber)
      verifyNoMoreInteractions(inductionSchedulePersistenceAdapter)
      verifyNoInteractions(inductionScheduleDateCalculationService)
      verifyNoInteractions(inductionScheduleEventService)
    }
  }

  @Nested
  inner class ReschedulePrisonersInductionSchedule {
    @Test
    fun `should reschedule prisoners induction schedule`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val prisonerAdmissionDate = TODAY

      val originalDueDate = TODAY.minusDays(5)
      val inductionSchedule = aValidInductionSchedule(
        prisonNumber = prisonNumber,
        scheduleStatus = EXEMPT_PRISONER_FAILED_TO_ENGAGE,
        deadlineDate = originalDueDate,
      )
      given(inductionSchedulePersistenceAdapter.getInductionSchedule(any())).willReturn(inductionSchedule)

      val expectedDueDate = prisonerAdmissionDate.plusDays(20)
      given(inductionScheduleDateCalculationService.determineCreateInductionScheduleDto(any(), any(), any(), any())).willReturn(
        aValidCreateInductionScheduleDto(
          prisonNumber = prisonNumber,
          deadlineDate = expectedDueDate,
        ),
      )

      val expectedInductionSchedule = inductionSchedule.copy(
        scheduleStatus = SCHEDULED,
        deadlineDate = expectedDueDate,
      )
      given(inductionSchedulePersistenceAdapter.updateInductionScheduleStatus(any())).willReturn(expectedInductionSchedule)

      val expectedUpdateInductionScheduleStatusDto = UpdateInductionScheduleStatusDto(
        prisonNumber = prisonNumber,
        reference = inductionSchedule.reference,
        scheduleStatus = SCHEDULED,
        exemptionReason = null,
        latestDeadlineDate = expectedDueDate,
        updatedAtPrison = PRISON_ID,
      )
      val expectedUpdatedInductionScheduleStatus = UpdatedInductionScheduleStatus(
        reference = inductionSchedule.reference,
        prisonNumber = prisonNumber,
        oldStatus = EXEMPT_PRISONER_FAILED_TO_ENGAGE,
        newStatus = SCHEDULED,
        exemptionReason = null,
        newDeadlineDate = expectedDueDate,
        oldDeadlineDate = originalDueDate,
        updatedAt = inductionSchedule.lastUpdatedAt,
        updatedBy = inductionSchedule.lastUpdatedBy,
        updatedAtPrison = inductionSchedule.lastUpdatedAtPrison,
      )

      // When
      val actual = service.reschedulePrisonersInductionSchedule(prisonNumber, prisonerAdmissionDate, PRISON_ID)

      // Then
      assertThat(actual).isEqualTo(expectedInductionSchedule)
      verify(inductionSchedulePersistenceAdapter).getInductionSchedule(prisonNumber)
      verify(inductionScheduleDateCalculationService).determineCreateInductionScheduleDto(prisonNumber, prisonerAdmissionDate, PRISON_ID, true)
      verify(inductionSchedulePersistenceAdapter).updateInductionScheduleStatus(expectedUpdateInductionScheduleStatusDto)
      verify(inductionScheduleEventService).inductionScheduleStatusUpdated(expectedUpdatedInductionScheduleStatus)
    }

    @Test
    fun `should not reschedule prisoners induction schedule given induction schedule is in a status not supported`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber, scheduleStatus = PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
      given(inductionSchedulePersistenceAdapter.getInductionSchedule(any())).willReturn(inductionSchedule)

      // When
      val exception = catchThrowableOfType(InvalidInductionScheduleStatusException::class.java) {
        service.reschedulePrisonersInductionSchedule(prisonNumber, TODAY, PRISON_ID)
      }

      // Then
      assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
      assertThat(exception.fromStatus).isEqualTo(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
      assertThat(exception.toStatus).isEqualTo(SCHEDULED)
      verify(inductionSchedulePersistenceAdapter).getInductionSchedule(prisonNumber)
    }

    @Test
    fun `should not reschedule prisoners induction schedule given prisoner does not have an induction schedule`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      given(inductionSchedulePersistenceAdapter.getInductionSchedule(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(InductionScheduleNotFoundException::class.java) {
        service.reschedulePrisonersInductionSchedule(prisonNumber, TODAY, PRISON_ID)
      }

      // Then
      assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
      verify(inductionSchedulePersistenceAdapter).getInductionSchedule(prisonNumber)
    }
  }
}
