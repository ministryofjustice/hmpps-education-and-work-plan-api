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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionScheduleHistory
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

  companion object {
    private val PRISON_NUMBER = randomValidPrisonNumber()
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
}
